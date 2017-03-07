import React, { Component, PropTypes } from 'react';
import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import TeamList from '../components/TeamList';
import TeamForm from '../components/TeamForm';
import * as teamActions from '../ducks/teams';

class TeamsContainer extends Component {

  componentDidMount() {
    this.props.teamActions.loadTeams();
  }

  render() {
    if (this.props.children) {
      return (
        <div className="wrapper">
          <section className="main">
            {this.props.children}
          </section>
        </div>
      );
    }
    return (
      <div className="wrapper">
        <section className="main">
          {(this.props.addingTeam || this.props.addTeamError) ?
            <TeamForm
              teams={this.props.teams}
              cancel={this.props.teamActions.addTeamToggle}
              create={this.props.teamActions.createTeam}
              addTeamError={this.props.addTeamError}
            /> :
            <div className="btn-toolbar">
              <button className="btn btn-sc" onClick={this.props.teamActions.addTeamToggle}>
                Create Team
              </button>
            </div>}
          <TeamList
            teams={this.props.teams}
            userTeams={this.props.auth.user.teams}
            teamActions={this.props.teamActions}
          />
        </section>
      </div>
    );
  }
}

TeamsContainer.propTypes = {
  children: PropTypes.object,
  teamActions: PropTypes.object.isRequired,
  auth: PropTypes.object.isRequired,
  teams: PropTypes.array.isRequired,
  addingTeam: PropTypes.bool.isRequired,
  addTeamError: PropTypes.oneOfType([
    PropTypes.bool, PropTypes.string,
  ]),
};

const mapStateToProps = state => ({
  auth: state.sc.auth,
  teams: state.sc.teams.teams,
  addTeamError: state.sc.teams.addTeamError,
  addingTeam: state.sc.teams.addingTeam,
});

const mapDispatchToProps = dispatch => ({
  teamActions: bindActionCreators(teamActions, dispatch),
});

export default connect(mapStateToProps, mapDispatchToProps)(TeamsContainer);
