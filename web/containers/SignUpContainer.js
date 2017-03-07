import React, { PropTypes } from 'react';
import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import SignUp from '../components/SignUp';
import * as authActions from '../ducks/auth';

const SignUpContainer = props => (
  <SignUp
    signUpError={props.signUpError}
    signUpSuccess={props.signUpSuccess}
    isSigningUp={props.isSigningUp}
    actions={props.actions}
    location={props.location}
  />
);

SignUpContainer.propTypes = {
  signUpError: PropTypes.string,
  signUpSuccess: PropTypes.bool,
  isSigningUp: PropTypes.bool,
  actions: PropTypes.object.isRequired,
  location: PropTypes.string,
};

const mapStateToProps = state => ({
  signUpError: state.sc.auth.signUpError,
  signUpSuccess: state.sc.auth.signUpSuccess,
  isSigningUp: state.sc.auth.isSigningUp,
});

const mapDispatchToProps = dispatch => ({
  actions: bindActionCreators(authActions, dispatch),
});

export default connect(mapStateToProps, mapDispatchToProps)(SignUpContainer);
