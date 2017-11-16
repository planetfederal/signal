import React, { Component, PropTypes } from "react";
import { bindActionCreators } from "redux";
import { connect } from "react-redux";
import find from "lodash/find";
import NotificationDetails from "../components/NotificationDetails";
import * as notificationActions from "../ducks/notifications";

class NotificationDetailsContainer extends Component {
  componentDidMount() {
    if (!this.props.notification) {
      this.props.actions.loadNotification(this.props.id);
    }
  }

  render() {
    return (
      <section className="main noPad">
        {this.props.notification ? (
          <NotificationDetails {...this.props} />
        ) : null}
      </section>
    );
  }
}

NotificationDetailsContainer.propTypes = {
  actions: PropTypes.object.isRequired,
  notification: PropTypes.object,
  id: PropTypes.string.isRequired
};

const mapStateToProps = (state, ownProps) => ({
  id: ownProps.params.id,
  notification: find(state.sc.notifications.notifications, {
    id: parseInt(ownProps.params.id)
  }),
  menu: state.sc.menu,
  errors: state.sc.notifications.errors
});

const mapDispatchToProps = dispatch => ({
  actions: bindActionCreators(notificationActions, dispatch)
});

// connect this "smart" container component to the redux store
export default connect(mapStateToProps, mapDispatchToProps)(
  NotificationDetailsContainer
);
