import React, { Component, PropTypes } from 'react';
import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import find from 'lodash/find';
import ProcessorNotification from '../components/ProcessorNotification';
import * as notificationActions from '../ducks/notifications';

class NotificationContainer extends Component {

  componentDidMount() {
    this.props.notificationActions.loadNotification(this.props.id);
  }

  render() {
    if (this.props.notification) {
      return <ProcessorNotification {...this.props} />;
    }
    return (
      <div className="wrapper">
        <section className="main">
          Loading
        </section>
      </div>
    );
  }
}

NotificationContainer.propTypes = {
  notificationActions: PropTypes.object.isRequired,
  id: PropTypes.string.isRequired,
  notification: PropTypes.object,
};

const mapStateToProps = (state, ownProps) => ({
  auth: state.sc.auth,
  id: ownProps.params.id,
  notification: find(state.sc.notifications.notifications, { id: +ownProps.params.id }),
  menu: state.sc.menu,
});

const mapDispatchToProps = dispatch => ({
  notificationActions: bindActionCreators(notificationActions, dispatch),
});

export default connect(mapStateToProps, mapDispatchToProps)(NotificationContainer);
