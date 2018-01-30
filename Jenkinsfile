node {
  withCredentials([string(credentialsId: 'boundlessgeoadmin-token', variable: 'GITHUB_TOKEN'), string(credentialsId: 'sonar-jenkins-pipeline-token', variable: 'SONAR_TOKEN')]) {

    currentBuild.result = "SUCCESS"

    try {
      stage('Checkout'){
        checkout scm
        sh """        
          echo "Running ${env.BUILD_ID} on ${env.JENKINS_URL}"
        """
      }

      stage('Linter'){
        sh """
          docker run -v \$(pwd -P):/code \
                     -w /code quay.io/boundlessgeo/sonar-maven-py3-alpine bash \
                     -e -c '. docker/devops/helper.sh && lint'
          """
      }

      stage('Set-Up'){
        // ensure docker volumes are cleared, build, wait for startup
        sh """
          docker-compose down
          docker rm -f \$(docker ps -aq) || echo "no containers to remove"
          docker system prune -f
          docker run -v \$(pwd -P):/code \
	            -w /code quay.io/boundlessgeo/bex-nodejs-bower-grunt bash \
	            -e
          docker-compose up --build --force-recreate -d
          echo "Waiting for signal to finish loading"
          """
      }

      stage('Unit-Tests'){
         // test
        sh """
	  docker ps
	  docker network ls
	  docker run -v \$(pwd -P):/web \
		-w /web clojure:lein-2.7.1 sh \
		-c 'bash -c "lein test :host 0.0.0.0" &&
		    bash -c "lein test2junit :host 0.0.0.0"'
          """
      }

      stage('Tear-Down'){
        // cleanup volumes
        sh """
          docker-compose down
          docker system prune -f
          """
      }

      if (env.BRANCH_NAME == 'master') {
        stage('SonarQube Analysis') {
          sh """
            docker run -e SONAR_HOST_URL='https://sonar-ciapi.boundlessgeo.io' \
                       -e SONAR_TOKEN=$SONAR_TOKEN \
                       -v \$(pwd -P):/code \
                       -w /code quay.io/boundlessgeo/sonar-maven-py3-alpine bash \
                       -c '. docker/devops/helper.sh && sonar-scan'
            """
        }
      }

    }
    catch (err) {

      currentBuild.result = "FAILURE"
        throw err
    } finally {
      // Success or failure, always send notifications
      echo currentBuild.result
      notifyBuild(currentBuild.result)
    }

  }
}


// Slack Integration

def notifyBuild(String buildStatus = currentBuild.result) {

  // generate a custom url to use the blue ocean endpoint
  def jobName =  "${env.JOB_NAME}".split('/')
  def repo = jobName[0]
  def pipelineUrl = "${env.JENKINS_URL}blue/organizations/jenkins/${repo}/detail/${env.BRANCH_NAME}/${env.BUILD_NUMBER}/pipeline"
  // Default values
  def colorName = 'RED'
  def colorCode = '#FF0000'
  def subject = "${buildStatus}\nJob: ${env.JOB_NAME}\nBuild: ${env.BUILD_NUMBER}\nJenkins: ${pipelineUrl}\n"
  def summary = (env.CHANGE_ID != null) ? "${subject}\nAuthor: ${env.CHANGE_AUTHOR}\n${env.CHANGE_URL}\n" : "${subject}"

  // Override default values based on build status
  if (buildStatus == 'SUCCESS') {
    colorName = 'GREEN'
    colorCode = '#228B22'
  }

  // Send notifications
  slackSend (color: colorCode, message: summary, channel: '#signal')
}
