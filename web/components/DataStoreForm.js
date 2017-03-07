import React, { Component, PropTypes } from 'react';
import uniqueId from 'lodash/uniqueId';
import { isUrl, emptyStore } from '../utils';

export const validate = (values) => {
  const errors = {};

  if (!values.name) {
    errors.name = 'Required';
  }
  if (!values.store_type) {
    errors.store_type = 'Required';
  }
  if (!values.version) {
    errors.version = 'Required';
  }
  if (!values.uri) {
    errors.uri = 'Must be valid uri';
  }
  if (values.store_type === 'wfs') {
    const validVersions = ['2.0.0', '1.1.0', '1.0.0'];
    if (validVersions.indexOf(values.version) < 0) {
      errors.version = `Valid WFS versions are: ${validVersions.join(', ')}`;
    }
    if (!values.uri) {
      errors.uri = 'Required';
    }
    if (!values.default_layers.length) {
      errors.default_layers = 'Must choose at least one default layer.';
    }
  }
  if (values.options && values.options.polling) {
    const msg = 'Must be a number of seconds between 1 and 600';
    const p = parseFloat(values.options.polling);
    if (!isNaN(p) && isFinite(values.options.polling)) {
      if (p % 1 !== 0 || p < 1 || p > 600) {
        errors.polling = msg;
      }
    } else {
      errors.polling = msg;
    }
  }
  return errors;
};

export class DataStoreForm extends Component {

  constructor(props) {
    super(props);
    this.state = {
      store: props.store,
      name: props.store.name,
      store_type: props.store.store_type,
      version: props.store.version,
      uri: props.store.uri,
      default_layers: props.store.default_layers || [],
      style: props.store.style && props.store.style.length ?
        props.store.style[0] : emptyStore.style[0],
      polling: props.store.options && props.store.options.polling ?
        props.store.options.polling : null,
      show_polling: props.store.store_type === 'geojson' || props.store.store_type === 'wfs',
    };

    this.onStoreTypeChange = this.onStoreTypeChange.bind(this);
    this.onNameChange = this.onNameChange.bind(this);
    this.onVersionChange = this.onVersionChange.bind(this);
    this.onURIChange = this.onURIChange.bind(this);
    this.onLayersChange = this.onLayersChange.bind(this);
    this.onPollingChange = this.onPollingChange.bind(this);
    this.onFillColorChange = this.onFillColorChange.bind(this);
    this.onFillOpacityChange = this.onFillOpacityChange.bind(this);
    this.onLineColorChange = this.onLineColorChange.bind(this);
    this.onLineOpacityChange = this.onLineOpacityChange.bind(this);
    this.onIconColorChange = this.onIconColorChange.bind(this);
    this.save = this.save.bind(this);
  }

  componentDidMount() {
    this.onURIChange();
  }

  onStoreTypeChange(e) {
    this.setState({
      store_type: e.target.value,
      show_polling: (e.target.value === 'geojson' || e.target.value === 'wfs'),
    });
    this.onURIChange();
  }

  onURIChange(e) {
    const uri = e ? e.target.value : this.state.uri;
    if (this.state.store_type === 'wfs') {
      if (isUrl(uri)) {
        this.props.actions.addStoreError('default_layers', 'Loading layers...');
        this.props.actions.getWFSLayers(uri);
      } else {
        this.props.actions.addStoreError('default_layers', 'Enter a URI to load layer list.');
      }
    }
    if (this.state.store_type !== 'wfs') {
      this.props.actions.addStoreError('default_layers', false);
      this.props.actions.updateWFSLayerList([]);
    }
    this.setState({ uri });
  }

  onLayersChange(e) {
    const chosenLayers = Array.from(e.target.options)
      .filter(option => option.selected)
      .map(option => option.value);
    this.setState({ default_layers: chosenLayers });
  }

  onNameChange(e) {
    this.setState({ name: e.target.value });
  }

  onVersionChange(e) {
    this.setState({ version: e.target.value });
  }

  onPollingChange(e) {
    this.setState({ polling: e.target.value });
  }

  onFillColorChange(e) {
    this.setState({
      style: {
        ...this.state.style,
        paint: {
          ...this.state.style.paint,
          'fill-color': e.target.value,
        },
      },
    });
  }

  onFillOpacityChange(e) {
    this.setState({
      style: {
        ...this.state.style,
        paint: {
          ...this.state.style.paint,
          'fill-opacity': e.target.value,
        },
      },
    });
  }

  onLineColorChange(e) {
    this.setState({
      style: {
        ...this.state.style,
        paint: {
          ...this.state.style.paint,
          'line-color': e.target.value,
        },
      },
    });
  }

  onLineOpacityChange(e) {
    this.setState({
      style: {
        ...this.state.style,
        paint: {
          ...this.state.style.paint,
          'line-opacity': e.target.value,
        },
      },
    });
  }

  onIconColorChange(e) {
    this.setState({
      style: {
        ...this.state.style,
        paint: {
          ...this.state.style.paint,
          'icon-color': e.target.value,
        },
      },
    });
  }

  save() {
    const store = {
      name: this.state.name,
      store_type: this.state.store_type,
      version: this.state.version.trim(),
      uri: this.state.uri.trim(),
      style: [this.state.style],
    };
    if (this.state.default_layers) {
      store.default_layers = this.state.default_layers;
    }
    if (this.state.polling) {
      store.options = {
        polling: this.state.polling.trim(),
      };
    }
    const errors = validate(store);
    this.props.actions.updateStoreErrors(errors);
    if (!Object.keys(errors).length) {
      this.props.onSubmit(this.props.store.id, store);
    }
  }

  render() {
    const { store, errors } = this.props;
    const paint = this.state.style.paint;
    return (
      <div className="side-form">
        <div className="form-group">
          <label htmlFor="store-name">Name:</label>
          <input
            type="text" id="store-name" className="form-control"
            value={this.state.name}
            onChange={this.onNameChange}
          />
          {errors.name ? <p className="text-danger">{errors.name}</p> : ''}
        </div>
        <div className="form-group">
          <label htmlFor="store-type">Type:</label>
          <select
            id="store-type" className="form-control" value={this.state.store_type}
            onChange={this.onStoreTypeChange}
          >
            <option value="">Select a type..</option>
            <option value="geojson">GeoJSON</option>
            <option value="gpkg">GeoPackage</option>
            <option value="wfs">WFS</option>
          </select>
          {errors.store_type ? <p className="text-danger">{errors.store_type}</p> : ''}
        </div>
        <div className="form-group">
          <label htmlFor="store-version">Version:</label>
          <input
            id="store-version" type="text" className="form-control"
            value={this.state.version} maxLength={15}
            onChange={this.onVersionChange}
          />
          {errors.version ? <p className="text-danger">{errors.version}</p> : ''}
        </div>
        <div className="form-group">
          <label htmlFor="store-uri">URI:</label>
          <input
            id="store-uri" type="text" className="form-control"
            value={this.state.uri} onChange={this.onURIChange}
          />
          {errors.uri ? <p className="text-danger">{errors.uri}</p> : ''}
        </div>
        {store.store_type === 'wfs' || this.state.store_type === 'wfs' ?
          <div className="form-group">
            <label htmlFor="default-layers">Default Layers:</label>
            <select
              id="default-layers"
              multiple className="form-control default_layers" value={this.state.default_layers}
              onChange={this.onLayersChange}
            >
              {this.props.layerList.map(layer => (
                <option value={layer} key={uniqueId()}>{layer}</option>
            ))}
            </select>
            {errors.default_layers ? <p className="text-danger">{errors.default_layers}</p> : ''}
          </div> : ''
        }
        {this.state.show_polling ?
          <div className="form-group">
            <label htmlFor="store-polling">Polling:</label>
            <p className="help-block">Number of seconds (1 - 600)</p>
            <input
              id="store-polling" type="text" className="form-control"
              value={this.state.polling} maxLength={5}
              onChange={this.onPollingChange}
            />
            {errors.polling ? <p className="text-danger">{errors.polling}</p> : ''}
          </div> : ''
        }
        <div className="form-group">
          <div className="multi-input-group">
            <div className="multi-input">
              <label htmlFor="store-fill-color">Fill Color:</label>
              <input
                id="store-fill-color" type="color" className="form-control"
                value={paint['fill-color']}
                onChange={this.onFillColorChange}
              />
            </div>
            <div className="multi-input">
              <label htmlFor="store-line-color">Line Color:</label>
              <input
                id="store-line-color" type="color" className="form-control"
                value={paint['line-color']}
                onChange={this.onLineColorChange}
              />
            </div>
            <div className="multi-input">
              <label htmlFor="store-icon-color">Icon Color:</label>
              <input
                id="store-icon-color" type="color" className="form-control"
                value={paint['icon-color']}
                onChange={this.onIconColorChange}
              />
            </div>
          </div>
        </div>
        <div className="form-group">
          <label htmlFor="store-fill-opacity">Fill Opacity:</label>
          <div className="input-group">
            <input
              id="store-fill-opacity" type="range" className="form-control"
              min="0" max="1" step=".1"
              value={paint['fill-opacity']}
              onChange={this.onFillOpacityChange}
            />
            <div className="range-value">
              {Number(paint['fill-opacity']).toFixed(1)}
            </div>
          </div>
        </div>
        <div className="form-group">
          <label htmlFor="store-line-opacity">Line Opacity:</label>
          <div className="input-group">
            <input
              id="store-line-opacity" type="range" className="form-control"
              min="0" max="1" step=".1"
              value={paint['line-opacity']}
              onChange={this.onLineOpacityChange}
            />
            <div className="range-value">
              {Number(paint['line-opacity']).toFixed(1)}
            </div>
          </div>
        </div>
        <div className="btn-toolbar">
          <button className="btn btn-sc" onClick={this.save}>Save</button>
          <button className="btn btn-default" onClick={this.props.cancel}>Cancel</button>
        </div>
      </div>
    );
  }
}

DataStoreForm.propTypes = {
  store: PropTypes.object.isRequired,
  onSubmit: PropTypes.func.isRequired,
  cancel: PropTypes.func.isRequired,
  errors: PropTypes.object.isRequired,
  layerList: PropTypes.array.isRequired,
  actions: PropTypes.object.isRequired,
};

export default DataStoreForm;
