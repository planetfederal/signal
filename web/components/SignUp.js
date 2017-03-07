import React, { Component, PropTypes } from 'react';
import { Link } from 'react-router';

class SignUp extends Component {

  constructor(props) {
    super(props);
    this.state = {
      name: '',
      email: '',
      password: '',
    };

    this.submit = this.submit.bind(this);
    this.emailChange = this.emailChange.bind(this);
    this.passwordChange = this.passwordChange.bind(this);
    this.nameChange = this.nameChange.bind(this);
  }

  submit(e) {
    e.preventDefault();
    this.props.actions.signUpUser(this.state.name, this.state.email, this.state.password);
  }

  emailChange(event) {
    this.setState({ email: event.target.value });
  }

  passwordChange(event) {
    this.setState({ password: event.target.value });
  }

  nameChange(event) {
    this.setState({ name: event.target.value });
  }

  renderErrorView() {
    return (
      <p>Error: {this.props.signUpError}</p>
    );
  }

  render() {
    return (
      <section className="main">
        <div className="side-form">
          {!!this.props.signUpError &&
            <div className="alert alert-danger">{ this.renderErrorView() }</div>
          }
          {this.props.signUpSuccess ?
            <div className="alert alert-info">
              <p>Sign up successful. <Link to="/login">Sign in</Link> with your new account.</p>
            </div> :
            <form role="form">
              <div className="form-group">
                <label htmlFor="name">Name</label>
                <input
                  type="text"
                  id="name"
                  className="form-control"
                  value={this.state.name}
                  onChange={this.nameChange}
                  disabled={this.props.isSigningUp}
                  placeholder="Name"
                />
              </div>
              <div className="form-group">
                <label htmlFor="email">Email</label>
                <input
                  type="email"
                  id="email"
                  className="form-control"
                  value={this.state.email}
                  onChange={this.emailChange}
                  disabled={this.props.isSigningUp}
                  placeholder="Email"
                />
              </div>
              <div className="form-group">
                <label htmlFor="password">Password</label>
                <input
                  type="password"
                  id="password"
                  className="form-control"
                  value={this.state.password}
                  onChange={this.passwordChange}
                  disabled={this.props.isSigningUp}
                  placeholder="Password"
                />
              </div>
              <button
                type="submit"
                className="btn btn-sc"
                disabled={this.props.isSigningUp}
                onClick={this.submit}
              >Sign Up</button>
            </form>
          }
        </div>
      </section>
    );
  }
}

SignUp.propTypes = {
  actions: PropTypes.object.isRequired,
  isSigningUp: PropTypes.bool.isRequired,
  signUpSuccess: PropTypes.bool.isRequired,
  signUpError: PropTypes.string.isRequired,
};

export default SignUp;
