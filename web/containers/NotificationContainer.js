import React, { Component, PropTypes } from "react";
import { bindActionCreators } from "redux";
import { connect } from "react-redux";
import find from "lodash/find";
import NotificationDetails from "../components/NotificationDetails";
import * as notificationActions from "../ducks/notifications";
import NotificationList from "../components/NotificationList";

class NotificationContainer extends Component {
  constructor(props) {
    super(props);
  }

  componentWillMount() {
    this.props.notificationActions.loadNotifications();
  }

  render() {
    const { children } = this.props;
    if (children) {
      return <div className="wrapper">{children}</div>;
    }
    if (this.props.notifications && this.props.notifications.length > 0) {
      return <NotificationList {...this.props} />;
    }
    return (
      <div className="wrapper">
        <section className="main">No Notifications</section>
      </div>
    );
  }
}

NotificationContainer.propTypes = {
  notificationActions: PropTypes.object.isRequired,
  id: PropTypes.string.isRequired,
  notification: PropTypes.object,
  childern: PropTypes.object
};

const mapStateToProps = (state, ownProps) => ({
  auth: state.sc.auth,
  id: ownProps.params.id,
  notifications: state.sc.notifications.notifications,
  menu: state.sc.menu
});

const mapDispatchToProps = dispatch => ({
  notificationActions: bindActionCreators(notificationActions, dispatch)
});

export default connect(mapStateToProps, mapDispatchToProps)(
  NotificationContainer
);
