import React, { PropTypes } from 'react';
import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import SignIn from '../components/SignIn';
import * as authActions from '../ducks/auth';

const SignInContainer = props => (
  <SignIn
    statusText={props.statusText}
    isAuthenticating={props.isAuthenticating}
    actions={props.actions}
    location={props.location}
  />
);

SignInContainer.propTypes = {
  actions: PropTypes.object.isRequired,
  isAuthenticating: PropTypes.bool.isRequired,
  statusText: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.bool,
  ]),
  location: PropTypes.object,
};

const mapStateToProps = state => ({
  isAuthenticating: state.sc.auth.isAuthenticating,
  statusText: state.sc.auth.statusText,
});

const mapDispatchToProps = dispatch => ({
  actions: bindActionCreators(authActions, dispatch),
});

export default connect(mapStateToProps, mapDispatchToProps)(SignInContainer);
