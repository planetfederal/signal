import React, { Component, PropTypes } from 'react';
import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import * as processorActions from '../ducks/processors';
import { FormList } from '../components/DataMapFormList';
import DataMap from '../components/DataMap';

class DataContainer extends Component {

  componentDidMount() {
    this.props.dataActions.loadDeviceLocations();
    this.props.processorActions.loadProcessors();
    this.props.formActions.loadForms()
      .then(() => {
        this.props.dataActions.loadFormDataAll();
      });
  }

  render() {
    return (
      <div className="data-map">
        <FormList {...this.props} />
        <DataMap {...this.props} />
      </div>
    );
  }
}

DataContainer.propTypes = {
  dataActions: PropTypes.object.isRequired,
  formActions: PropTypes.object.isRequired,
  processorActions: PropTypes.object.isRequired,
};

const mapStateToProps = state => ({
  formData: state.sc.data.formData,
  form_ids: state.sc.data.form_ids,
  forms: state.sc.forms.forms,
  device_locations: state.sc.data.device_locations,
  deviceLocationsOn: state.sc.data.deviceLocationsOn,
  spatialProcessorsOn: state.sc.data.spatialProcessorsOn,
  spatial_processors: state.sc.processors.spatial_processors,
  menu: state.sc.menu,
});

const mapDispatchToProps = dispatch => ({
  processorActions: bindActionCreators(processorActions, dispatch),
});

export default connect(mapStateToProps, mapDispatchToProps)(DataContainer);
