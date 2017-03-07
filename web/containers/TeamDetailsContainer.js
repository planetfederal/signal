import React, { PropTypes } from 'react';
import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import find from 'lodash/find';
import Team from '../components/Team';
import * as teamActions from '../ducks/teams';

const TeamDetailsContainer = props => (
  <div>
    {!!props.team &&
      <div>
        <Team
          team={props.team}
          userTeams={props.auth.user.teams}
          teamActions={props.teamActions}
        />
        <div className="btn-toolbar">
          <button
            className="btn btn-danger"
            onClick={() => { props.teamActions.deleteTeam(props.team.id); }}
          >
            Delete Team
          </button>
        </div>
      </div>
    }
  </div>
);

TeamDetailsContainer.propTypes = {
  auth: PropTypes.object.isRequired,
  teamActions: PropTypes.object.isRequired,
  teams: PropTypes.array.isRequired,
  team: PropTypes.object,
};

const mapStateToProps = (state, ownProps) => ({
  auth: state.sc.auth,
  id: ownProps.params.id,
  teams: state.sc.teams.teams,
  team: find(state.sc.teams.teams, { id: +ownProps.params.id }),
});

const mapDispatchToProps = dispatch => ({
  teamActions: bindActionCreators(teamActions, dispatch),
});

export default connect(mapStateToProps, mapDispatchToProps)(TeamDetailsContainer);
