
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
      stage('Migrations'){
        sh """
          echo "Migration Set-UP - Create DBs"
          createuser signal --createdb -h localhost -U postgres
          createdb signal -O signal -h localhost -U postgres
          psql -U postgres -d signal -c "CREATE EXTENSION IF NOT EXISTS pgcrypto" -h localhost
          psql -U postgres -d signal -c "CREATE EXTENSION IF NOT EXISTS postgis" -h localhost
          psql -U signal   -d signal -c "CREATE SCHEMA IF NOT EXISTS signal" -h localhost
          echo "Migration - Run lein migrate"
          lein migrate
        """  
      }
      stage('LinterLein'){
        sh """
          echo "Generate Sample Data"
          lein sampledata
          echo "Linter"
          lein eastwood 
          echo "Run Tests"
          lein test
          echo "Code Coverage - Report"
          lein cloverage
          echo "Formatter"
          lein cljfmt fix
          echo "Generate Docs"
          lein codox

        """
      }
      stage('Deploy'){
        sh """
          docker build -t quay.io/boundlessgeo/signal:devio .
          docker push quay.io/boundlessgeo/signal:devio
          cf push signal -o quay.io/boundlessgeo/signal:devio
        """
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
  slackSend (color: colorCode, message: summary, channel: '#exchange-bots')
}