import React, { Component, PropTypes } from 'react';
import dateFormat from 'date-fns/format';
import '../style/DataMap.less';

const style = {
  map: {
    flex: 1,
  },
  popup: {
    background: 'white',
    padding: 10,
    borderWidth: 2,
    borderRadius: 3,
    borderColor: '#333',
  },
  thumbnail: {
    height: 100,
  },
};

const iconStyle = new ol.style.Style({
  image: new ol.style.Icon(/** @type {olx.style.IconOptions} */ ({
    anchor: [0.5, 46],
    anchorXUnits: 'fraction',
    anchorYUnits: 'pixels',
    src: 'marker-icon.png',
  })),
});

const deviceStyle = new ol.style.Style({
  image: new ol.style.Icon(/** @type {olx.style.IconOptions} */ ({
    anchor: [0.5, 20],
    anchorXUnits: 'fraction',
    anchorYUnits: 'pixels',
    src: 'mobile.png',
    size: [41, 41],
  })),
});

const processorStyle = new ol.style.Style({
  fill: new ol.style.Fill({
    color: 'rgba(255, 0, 0, 0.1)',
  }),
  stroke: new ol.style.Stroke({
    color: '#f00',
    width: 1,
  }),
});


const format = new ol.format.GeoJSON();

class DataMap extends Component {

  static makeFieldValue(field, value) {
    if (field.type === 'photo') {
      return (
        <img
          className="img img-thumbnail"
          alt="form-submission" style={style.thumbnail} src={value}
        />);
    }
    return value;
  }

  static makePopupTableDeviceLocation(f) {
    const table = (<div>
      <p className="form-label">Device Location</p>
      <table className="table table-bordered table-striped"><tbody>
        <tr><td>Identifier:</td><td>{f.metadata.identifier}</td></tr>
        {typeof f.metadata.device_info === 'string' ?
          <tr><td>Device Info:</td><td>{f.metadata.device_info}</td></tr> : null}
        {typeof f.metadata.device_info.os === 'string' ?
          <tr><td>OS:</td><td>{f.metadata.device_info.os}</td></tr> : null}
        {typeof f.metadata.updated_at === 'string' ?
          <tr>
            <td>Time Recorded:</td>
            <td>{dateFormat(f.metadata.updated_at, 'dddd, MMMM Do YYYY, h:mm:ss a')}</td>
          </tr> : null}
      </tbody></table>
    </div>);
    return table;
  }

  constructor(props) {
    super(props);
    this.state = {
      activeFeature: false,
    };
  }

  componentDidMount() {
    this.createMap();
    window.addEventListener('resize', () => {
      this.map.updateSize();
    });
  }

  componentWillReceiveProps(nextProps) {
    if (this.props.menu.open !== nextProps.menu.open) {
      // wait for menu to transition
      setTimeout(() => this.map.updateSize(), 200);
    }
    this.renderFeatures(nextProps);
  }

  componentWillUnmount() {
    if (this.connection) {
      this.connection.close();
    }
  }

  createMap() {
    while (this.mapRef.firstChild) {
      this.mapRef.removeChild(this.mapRef.firstChild);
    }
    this.vectorSource = new ol.source.Vector();
    this.deviceLocationsSource = new ol.source.Vector();
    this.spatialProcessorsSource = new ol.source.Vector();
    const vectorLayer = new ol.layer.Vector({
      source: this.vectorSource,
    });
    const deviceLocationsLayer = new ol.layer.Vector({
      source: this.deviceLocationsSource,
    });
    const spatialProcessorsLayer = new ol.layer.Vector({
      source: this.spatialProcessorsSource,
      style: processorStyle,
    });
    this.map = new ol.Map({
      target: this.mapRef,
      layers: [
        new ol.layer.Tile({
          source: new ol.source.OSM(),
        }),
        vectorLayer,
        deviceLocationsLayer,
        spatialProcessorsLayer,
      ],
      view: new ol.View({
        center: ol.proj.fromLonLat([-100, 30]),
        zoom: 3,
      }),
    });
    const popup = new ol.Overlay({
      element: this.popup,
      positioning: 'bottom-center',
      stopEvent: false,
      offset: [0, -50],
    });
    const selectInteraction = new ol.interaction.Select({
      layers: [vectorLayer, deviceLocationsLayer],
    });
    this.map.addInteraction(selectInteraction);
    selectInteraction.on('select', (e) => {
      if (e.selected.length) {
        const feature = e.selected[0];
        const gj = JSON.parse(format.writeFeature(feature));
        const c = feature.getGeometry().getCoordinates();
        this.map.addOverlay(popup);
        popup.setPosition(c);
        if (gj.id.indexOf('form_submission') > -1) {
          const data = this.props.formData
            .filter(fd => fd.id === +gj.id.replace('form_submission.', ''));
          this.setState({ activeFeature: data[0] });
        }
        if (gj.id.indexOf('device_location') > -1) {
          const data = this.props.device_locations
            .filter(fd => fd.id === +gj.id.replace('device_location.', ''));
          this.setState({ deviceLocationActive: data[0] });
        }
      } else {
        this.setState({
          activeFeature: false,
          deviceLocationActive: false,
        });
        this.map.removeOverlay(popup);
      }
    });
    this.map.on('pointermove', (evt) => {
      const pixel = this.map.getEventPixel(evt.originalEvent);
      const hit = this.map.hasFeatureAtPixel(pixel);
      this.map.getTarget().style.cursor = hit ? 'pointer' : '';
    });
  }

  makePopupTableFormSubmission(f) {
    const form = this.props.forms[f.form_key];
    const rows = form.fields.map(field => (
      <tr key={field.field_key}>
        <td className="form-label">{field.field_label}</td>
        <td>{DataMap.makeFieldValue(field, f.val.properties[field.field_key])}</td>
      </tr>
    ));
    const table = (<div>
      <p className="form-label">{form.form_label}</p>
      <p className="form-note">{f.val.metadata.created_at}</p>
      <table className="table table-bordered table-striped"><tbody>{rows}</tbody></table>
    </div>);
    return table;
  }

  renderFeatures(props) {
    this.vectorSource.clear();
    const features = props.formData
      .filter(f => f.val.geometry)
      .filter(f => props.form_ids.indexOf(f.form_id) >= 0).map((f) => {
        const feature = format.readFeature(f.val);
        feature.setId(`form_submission.${f.id}`);
        feature.getGeometry().transform('EPSG:4326', 'EPSG:3857');
        feature.setStyle(iconStyle);
        return feature;
      });
    this.vectorSource.addFeatures(features);

    this.deviceLocationsSource.clear();
    if (props.deviceLocationsOn) {
      const deviceLocationFeatures = props.device_locations
        .map((f) => {
          const feature = format.readFeature(f);
          feature.setId(f.metadata.identifier);
          feature.getGeometry().transform('EPSG:4326', 'EPSG:3857');
          feature.setStyle(deviceStyle);
          return feature;
        });
      this.deviceLocationsSource.addFeatures(deviceLocationFeatures);
    }

    this.spatialProcessorsSource.clear();
    if (props.spatialProcessorsOn) {
      Object.keys(props.spatial_processors)
        .map(k => props.spatial_processors[k])
        .filter(t => t.rules.length)
        .forEach((t, i) => {
          t.rules
          .filter(r => r && typeof r === 'object' && r.rhs)
          .forEach((r, j) => {
            const gj = r.rhs;
            gj.id = `${t.id}.${i}.${j}`;
            const processorFeatures = format.readFeatures(gj);
            processorFeatures.forEach((feature) => {
              feature.getGeometry().transform('EPSG:4326', 'EPSG:3857');
            });
            this.spatialProcessorsSource.addFeatures(processorFeatures);
          });
        });
    }
  }

  render() {
    let table;
    if (this.state.activeFeature) {
      table = this.makePopupTableFormSubmission(this.state.activeFeature);
    } else if (this.state.deviceLocationActive) {
      table = DataMap.makePopupTableDeviceLocation(this.state.deviceLocationActive);
    } else {
      table = <table />;
    }
    return (
      <div className="map" ref={(c) => { this.mapRef = c; }} style={style.map}>
        <div className="popup" ref={(c) => { this.popup = c; }} style={style.popup}>{table}</div>
      </div>
    );
  }
}

DataMap.propTypes = {
  menu: PropTypes.object.isRequired,
  forms: PropTypes.object.isRequired,
  formData: PropTypes.array.isRequired,
  device_locations: PropTypes.array.isRequired,
};

export default DataMap;
