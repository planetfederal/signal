import React, { Component, PropTypes } from 'react';
import find from 'lodash/find';
import { toKey } from '../utils';
import '../style/FormDetails.less';

const fieldOptions = ['field_label', 'field_key', 'is_required'];

const fieldConstraints = {
  string: ['initial_value', 'minimum_length', 'maximum_length', 'pattern'],
  select: ['options'],
  number: ['initial_value',
    'minimum', 'maximum', 'is_integer'],
  boolean: [],
  date: [],
  slider: ['initial_value', 'minimum', 'maximum'],
  counter: ['initial_value', 'minimum', 'maximum'],
  photo: [],
};

const fieldLabels = {
  field_key: 'Field Key',
  field_label: 'Field Label',
  is_required: 'Required',
  is_integer: 'Integer',
  initial_value: 'Default Value',
  minimum_length: 'Minimum Length',
  maximum_length: 'Maximum Length',
  pattern: 'Validation Pattern',
  options: 'Options',
  minimum: 'Minimum',
  maximum: 'Maximum',
  exclusive_minimum: 'Exclusive Minimum',
  exclusive_maximum: 'Exclusive Maximum',
};

class FieldOptions extends Component {

  constructor(props) {
    super(props);
    this.removeField = this.removeField.bind(this);
  }

  changeOption(field, option, e, updater) {
    let value;
    if (e.currentTarget.type === 'checkbox') {
      value = e.target.checked;
    } else if (option === 'options') {
      value = e.target.value.split('\n');
    } else if (option === 'field_label') {
      value = e.target.value;
      updater(
        this.props.form.form_key,
        field.id,
        'field_key',
        toKey(value),
      );
    } else {
      value = e.target.value;
    }
    updater(
      this.props.form.form_key,
      field.id,
      option,
      value,
    );
  }

  removeField(field) {
    this.props.removeField(
      this.props.form.form_key,
      field.id,
    );
  }

  makeOptionInput(field, option, value, i, updater) {
    if (option === 'is_integer' || option === 'is_required') {
      return (
        <div className="checkbox" key={option + i}>
          <label htmlFor={option}>
            <input
              type="checkbox"
              id={option}
              checked={value && value === true}
              onChange={(e) => { this.changeOption(field, option, e, updater); }}
            /> {fieldLabels[option]}
          </label>
        </div>
      );
    }
    if (option === 'options') {
      return (
        <div className="form-group" key={option + i}>
          <label htmlFor={option}>{fieldLabels[option]}</label>
          <textarea
            className="form-control" rows="3"
            id={option}
            onChange={(e) => { this.changeOption(field, option, e, updater); }}
            value={value ? value.join('\n') : ''}
          />
        </div>
      );
    }
    return (
      <div className="form-group" key={option + i}>
        <label htmlFor={option}>{fieldLabels[option]}</label>
        <input
          type="text" className="form-control"
          id={option}
          value={value || ''}
          onChange={(e) => { this.changeOption(field, option, e, updater); }}
        />
      </div>
    );
  }

  makeOptionInputs(field) {
    const options = fieldOptions.map((o, i) =>
      this.makeOptionInput(field, o, field[o], i, this.props.updateFieldOption));
    if (field.constraints) {
      const constraints = fieldConstraints[field.type].map((o, i) =>
        this.makeOptionInput(field, o, field.constraints[o], i, this.props.updateFieldConstraint),
      );
      return options.concat(constraints);
    }
    return options;
  }

  render() {
    const { form, activeField } = this.props;
    const field = find(form.fields, { field_key: activeField });
    if (activeField && field) {
      const optionInputs = this.makeOptionInputs(field);
      return (
        <div className="form-options form-pane">
          <div className="form-pane-title"><h5>Field Options</h5></div>
          <div className="form-pane-wrapper">
            {optionInputs}
            <button className="btn btn-danger" onClick={() => this.removeField(field)}>
              Delete Field
            </button>
          </div>
        </div>
      );
    }
    return (
      <div className="form-options form-pane">
        <div className="form-pane-title"><h5>Field Options</h5></div>
        <div className="form-pane-wrapper">
          <p className="warning-message">Select field.</p>
        </div>
      </div>
    );
  }
}

FieldOptions.propTypes = {
  form: PropTypes.object.isRequired,
  activeField: PropTypes.string,
  updateFieldOption: PropTypes.func.isRequired,
  updateFieldConstraint: PropTypes.func.isRequired,
  removeField: PropTypes.func.isRequired,
};

export default FieldOptions;
