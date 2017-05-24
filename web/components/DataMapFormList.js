import React, { Component, PropTypes } from 'react';
import classNames from 'classnames';
import values from 'lodash/values';

const FormListItem = ({ active, onClick, title, count }) =>
  <div
    className={classNames('data-form-list-item', { active })}
    onClick={onClick}
  >
    <input type="checkbox" checked={active} />
    <h4>{title}</h4><div className="count">({count})</div>
  </div>;

FormListItem.propTypes = {
  active: PropTypes.bool.isRequired,
  onClick: PropTypes.func.isRequired,
  title: PropTypes.string.isRequired,
  count: PropTypes.number.isRequired,
};

export class FormList extends Component {

  constructor(props) {
    super(props);

    this.toggleForm = this.toggleForm.bind(this);
    this.toggleDeviceLocations = this.toggleDeviceLocations.bind(this);
    this.toggleSpatialProcessors = this.toggleSpatialProcessors.bind(this);
  }

  toggleForm(form) {
    if (this.props.form_ids.indexOf(form.id) >= 0) {
      this.props.dataActions.removeFormId(form.id);
    } else {
      this.props.dataActions.addFormId(form.id);
    }
  }

  toggleDeviceLocations() {
    this.props.dataActions.toggleDeviceLocations(!this.props.deviceLocationsOn);
  }

  toggleSpatialProcessors() {
    this.props.dataActions.toggleSpatialProcessors(!this.props.spatialProcessorsOn);
  }

  render() {
    return (
      <div className="data-form-list">

        {values(this.props.forms)
          .map((f) => {
            const count = this.props.formData.filter(fd => fd.form_id === f.id).length;
            const active = this.props.form_ids.indexOf(f.id) >= 0;
            return (<FormListItem
              key={f.id} active={active} title={f.form_label} count={count}
              onClick={() => { this.toggleForm(f); }}
            />);
          })
        }
        <FormListItem
          key={'device_locations'}
          active={this.props.deviceLocationsOn}
          title={'Device Locations'}
          count={this.props.device_locations.length}
          onClick={this.toggleDeviceLocations}
        />
        <FormListItem
          key={'spatial_processors'}
          active={this.props.spatialProcessorsOn}
          title={'Spatial Processors'}
          count={Object.keys(this.props.spatial_processors).length}
          onClick={this.toggleSpatialProcessors}
        />

      </div>
    );
  }
}

FormList.propTypes = {
  spatialProcessorsOn: PropTypes.bool.isRequired,
  deviceLocationsOn: PropTypes.bool.isRequired,
  spatial_processors: PropTypes.object.isRequired,
  device_locations: PropTypes.array.isRequired,
  formData: PropTypes.array.isRequired,
  form_ids: PropTypes.array.isRequired,
  forms: PropTypes.object.isRequired,
  dataActions: PropTypes.object.isRequired,
};

export default FormList;
