import React, { Component, PropTypes } from 'react';
import without from 'lodash/without';
import { isEmail } from '../utils';

export const validate = (values) => {
  const errors = {};

  if (!values.name) {
    errors.name = 'Required';
  }

  values.recipients.emails.forEach((email) => {
    if (!isEmail(email)) {
      errors.email = 'Invalid email address';
    }
  });

  return errors;
};

export class TriggerForm extends Component {

  constructor(props) {
    super(props);
    this.state = {
      repeated: props.trigger.repeated,
      sourceStores: props.trigger.stores || [],
      email_recipients: props.trigger.recipients.emails || [],
      name: props.trigger.name,
      description: props.trigger.description,
    };

    this.save = this.save.bind(this);
    this.onOptionChange = this.onOptionChange.bind(this);
    this.onSourceChange = this.onSourceChange.bind(this);
    this.onEmailChange = this.onEmailChange.bind(this);
    this.onNameChange = this.onNameChange.bind(this);
    this.onDescriptionChange = this.onDescriptionChange.bind(this);
  }

  onOptionChange(e) {
    this.setState({ repeated: e.target.value === 'repeat_on' });
  }

  onNameChange(e) {
    this.setState({ name: e.target.value });
  }

  onSourceChange(e) {
    if (e.target.checked) {
      this.setState({
        sourceStores: this.state.sourceStores.concat(e.target.value),
      });
    } else {
      this.setState({
        sourceStores: without(this.state.sourceStores, e.target.value),
      });
    }
  }

  onEmailChange(e) {
    this.setState({
      email_recipients: e.target.value.split('\n'),
    });
  }

  onDescriptionChange(e) {
    this.setState({ description: e.target.value });
  }

  save() {
    const newTrigger = {
      ...this.props.trigger,
      name: this.state.name,
      description: this.state.description,
      repeated: this.state.repeated,
      stores: this.state.sourceStores,
      recipients: {
        emails: this.state.email_recipients,
        devices: [],
      },
    };
    const errors = validate(newTrigger);
    this.props.actions.updateTriggerErrors(errors);
    if (!Object.keys(errors).length) {
      this.props.onSave(newTrigger);
    }
  }

  render() {
    const { errors, cancel, stores } = this.props;
    return (
      <div className="side-form">
        <div className="form-group">
          <label htmlFor="trigger-name">Name:</label>
          <input
            id="trigger-name" type="text" className="form-control"
            onChange={this.onNameChange}
            value={this.state.name}
          />
          {errors.name ? <p className="text-danger">{errors.name}</p> : ''}
        </div>
        <div className="form-group">
          <label htmlFor="trigger-description">Description:</label>
          <textarea
            id="trigger-description"
            className="form-control" rows="3"
            onChange={this.onDescriptionChange}
            value={this.state.description}
          />
          {errors.description ? <p className="text-danger">{errors.description}</p> : ''}
        </div>
        <div className="form-group">
          <label htmlFor="trigger-source">Source Store:</label>
          {Object.keys(stores).map(id => (
            <div className="checkbox">
              <label htmlFor={id}>
                <input
                  id={id}
                  type="checkbox" checked={this.state.sourceStores.indexOf(id) >= 0}
                  value={id}
                  onChange={this.onSourceChange}
                /> {stores[id].name}
              </label>
            </div>
            ))}
        </div>
        <div className="form-group">
          <label htmlFor="store-repeated">Repeated:</label>
          <div className="radio">
            <label htmlFor="repeat_off">
              <input
                type="radio" name="repeated" id="repeat_off" value="repeat_off"
                checked={!this.state.repeated}
                onChange={this.onOptionChange}
              />
              Alert Once
            </label>
          </div>
          <div className="radio">
            <label htmlFor="repeat_on">
              <input
                type="radio" name="repeated" id="repeat_on" value="repeat_on"
                defaultChecked={this.state.repeated}
                onChange={this.onOptionChange}
              />
              Alert Always
            </label>
          </div>
        </div>
        <div className="form-group">
          <label htmlFor="recipients">Email Recipients</label>
          <textarea
            id="recipients"
            className="form-control" rows="3"
            onChange={this.onEmailChange}
            value={this.state.email_recipients.join('\n')}
          />
          {errors.email ? <p className="text-danger">{errors.email}</p> : ''}
        </div>
        {!!this.props.errors.length &&
          <p className="text-danger">{this.props.errors}</p>}
        <div className="btn-toolbar">
          <button className="btn btn-sc" onClick={this.save}>Save</button>
          <button className="btn btn-default" onClick={cancel}>Cancel</button>
        </div>
      </div>
    );
  }
}

TriggerForm.propTypes = {
  trigger: PropTypes.object.isRequired,
  errors: PropTypes.object.isRequired,
  cancel: PropTypes.func.isRequired,
  stores: PropTypes.object.isRequired,
  actions: PropTypes.object.isRequired,
  onSave: PropTypes.func.isRequired,
};

export default TriggerForm;
