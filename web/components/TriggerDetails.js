import React, { Component, PropTypes } from 'react';
import Dropzone from 'react-dropzone';
import isEqual from 'lodash/isEqual';
import isEmpty from 'lodash/isEmpty';
import TriggerItem from './TriggerItem';
import { TriggerForm } from './TriggerForm';
import PropertyListItem from './PropertyListItem';
import '../style/Triggers.less';

const format = new ol.format.GeoJSON();

const triggerStyle = new ol.style.Style({
  fill: new ol.style.Fill({
    color: 'rgba(255, 0, 0, 0.1)',
  }),
  stroke: new ol.style.Stroke({
    color: '#f00',
    width: 1,
  }),
});

const newRuleStyle = new ol.style.Style({
  fill: new ol.style.Fill({
    color: 'rgba(0, 0, 255, 0.1)',
  }),
  stroke: new ol.style.Stroke({
    color: '#00f',
    width: 1,
  }),
});

class TriggerDetails extends Component {
  constructor(props) {
    super(props);
    this.state = {
      addingRule: false,
      editing: false,
      editingRule: false,
      editingTrigger: false,
      creating: false,
      drawing: false,
      uploading: false,
      fileUploaded: false,
      uploadedFile: false,
      uploadErr: false,
      rule_comparator: '$geowithin',
      activeRules: {},
    };
    this.ruleLayers = {};
    this.onSave = this.onSave.bind(this);
    this.onCancel = this.onCancel.bind(this);
    this.onDraw = this.onDraw.bind(this);
    this.onUpload = this.onUpload.bind(this);
    this.onDrop = this.onDrop.bind(this);
    this.onDelete = this.onDelete.bind(this);
    this.onAddRule = this.onAddRule.bind(this);
    this.onRuleComparatorChange = this.onRuleComparatorChange.bind(this);
    this.onEditTrigger = this.onEditTrigger.bind(this);
    this.onCancelEditTrigger = this.onCancelEditTrigger.bind(this);
    this.onEditTriggerSave = this.onEditTriggerSave.bind(this);
    this.toggleRule = this.toggleRule.bind(this);
    this.onEditRule = this.onEditRule.bind(this);
    this.onDeleteRule = this.onDeleteRule.bind(this);
  }

  componentDidMount() {
    this.createMap();
    window.addEventListener('resize', () => {
      this.createMap();
    });
  }

  componentWillReceiveProps(nextProps) {
    if (!isEqual(nextProps.trigger.rules, this.props.trigger.rules)) {
      this.addRules(nextProps.trigger);
    }
    if (this.props.menu.open !== nextProps.menu.open) {
      // wait for menu to transition
      setTimeout(() => this.map.updateSize(), 200);
    }
  }

  onCancel() {
    this.map.removeInteraction(this.modify);
    this.map.removeInteraction(this.create);
    this.select.getFeatures().clear();
    this.newRuleSource.clear();
    this.setState({
      editing: false,
      creating: false,
      drawing: false,
      uploading: false,
      fileUploaded: false,
      uploadErr: false,
      uploadedFile: false,
      editingRule: false,
      editingTrigger: false,
    });
  }

  onSave() {
    this.setState({
      editing: false,
      creating: false,
      drawing: false,
      uploading: false,
      uploadedFile: false,
    });
    this.map.removeInteraction(this.modify);
    this.map.removeInteraction(this.create);
    this.select.getFeatures().clear();
    const fcId = `${this.props.trigger.id}.${this.props.trigger.rules.length + 1}`;
    const fs = this.newRuleSource.getFeatures().map((f, i) => {
      f.setId(`${fcId}.${i}`);
      return f;
    });
    const gj = JSON.parse(format.writeFeatures(fs, {
      dataProjection: 'EPSG:4326',
      featureProjection: 'EPSG:3857',
    }));
    gj.id = fcId;
    gj.features = gj.features.map(f => ({
      ...f,
      properties: {},
    }));
    const newRule = {
      lhs: ['geometry'],
      comparator: this.state.rule_comparator,
      rhs: gj,
      id: Date.now(),
    };
    const newTrigger = {
      ...this.props.trigger,
      rules: this.props.trigger.rules ? this.props.trigger.rules.concat(newRule) : [newRule],
    };
    this.newRuleSource.clear();
    this.props.actions.updateTrigger(newTrigger);
  }

  onDraw() {
    this.setState({ drawing: true });
    this.map.addInteraction(this.create);
  }

  onUpload() {
    this.setState({ uploading: true });
  }

  onDrop(acceptedFiles) {
    if (acceptedFiles.length) {
      const file = acceptedFiles[0];
      const reader = new FileReader();
      reader.onload = (e) => {
        try {
          const gj = JSON.parse(e.target.result);
          this.setState({
            uploadErr: false,
            uploadedFile: file.name,
          });
          const features = format.readFeatures(gj);
          features.forEach((feature) => {
            feature.getGeometry().transform('EPSG:4326', 'EPSG:3857');
            this.newRuleSource.addFeature(feature);
          });
          this.map.getView().fit(this.newRuleSource.getExtent(), this.map.getSize());
        } catch (err) {
          this.setState({ uploadErr: 'Not valid GeoJSON' });
        }
      };
      reader.readAsText(file);
    }
  }

  onDelete() {
    this.props.actions.deleteTrigger(this.props.trigger);
  }

  onRuleComparatorChange(e) {
    this.setState({
      rule_comparator: e.target.value,
    });
  }

  onAddRule() {
    this.setState({ creating: true });
  }

  onEditTrigger() {
    this.setState({ editingTrigger: true });
  }

  onCancelEditTrigger() {
    this.setState({ editingTrigger: false }, () => {
      this.createMap();
    });
  }

  onEditTriggerSave(trigger) {
    this.props.actions.updateTrigger(trigger);
    this.setState({ editingTrigger: false }, () => {
      this.createMap();
    });
  }

  onEditRule(rule) {
    const layer = this.ruleLayers[rule.id];
    const fs = layer.getSource().getFeatures().map(f => f.clone());
    this.setState({ editingRule: rule.id });
    this.select.getFeatures().clear();
    this.newRuleSource.clear();
    this.map.getView().fit(layer.getSource().getExtent(), this.map.getSize());
    this.map.removeLayer(layer);
    this.newRuleSource.addFeatures(fs);
    this.modify = new ol.interaction.Modify({
      features: new ol.Collection(this.newRuleSource.getFeatures()),
    });
    this.map.addInteraction(this.modify);
  }

  onSaveRule(rule) {
    const fcId = `${this.props.trigger.id}.${rule.id}`;
    const fs = this.newRuleSource.getFeatures().map((f, i) => {
      f.setId(`${fcId}.${i}`);
      return f;
    });
    const gj = JSON.parse(format.writeFeatures(fs, {
      dataProjection: 'EPSG:4326',
      featureProjection: 'EPSG:3857',
    }));
    gj.id = fcId;
    gj.features = gj.features.map(f => ({
      ...f,
      properties: {},
    }));
    const newRule = {
      ...rule,
      rhs: gj,
    };
    const newTrigger = {
      ...this.props.trigger,
      rules: this.props.trigger.rules.map((r) => {
        if (r.id === newRule.id) {
          return newRule;
        }
        return r;
      }),
    };
    this.setState({
      editingRule: false,
    });
    this.map.removeInteraction(this.modify);
    this.newRuleSource.clear();
    this.props.actions.updateTrigger(newTrigger);
  }

  onDeleteRule(rule) {
    const newTrigger = {
      ...this.props.trigger,
      rules: this.props.trigger.rules.filter(r => r.id !== rule.id),
    };
    this.select.getFeatures().clear();
    this.props.actions.updateTrigger(newTrigger);
  }

  onCancelRule(rule) {
    const layer = this.ruleLayers[rule.id];
    this.map.removeInteraction(this.modify);
    this.newRuleSource.clear();
    this.map.addLayer(layer);
    this.setState({
      editingRule: false,
    });
  }

  createMap() {
    while (this.mapRef.firstChild) {
      this.mapRef.removeChild(this.mapRef.firstChild);
    }
    this.allRuleSource = new ol.source.Vector();
    this.newRuleSource = new ol.source.Vector();
    const newRuleLayer = new ol.layer.Vector({
      source: this.newRuleSource,
      style: newRuleStyle,
    });
    this.select = new ol.interaction.Select({
      wrapX: false,
      style: newRuleStyle,
    });
    this.modify = new ol.interaction.Modify({
      features: new ol.Collection(this.newRuleSource.getFeatures()),
    });
    this.create = new ol.interaction.Draw({
      source: this.newRuleSource,
      type: ('Polygon'),
    });
    this.map = new ol.Map({
      target: this.mapRef,
      interactions: ol.interaction.defaults().extend([this.select]),
      layers: [
        new ol.layer.Tile({
          source: new ol.source.OSM(),
        }),
        newRuleLayer,
      ],
      view: new ol.View({
        center: ol.proj.fromLonLat([-100, 30]),
        zoom: 3,
      }),
    });

    this.addRules(this.props.trigger);
  }

  addRules(trigger) {
    Object.keys(this.ruleLayers).forEach(layerid =>
      this.map.removeLayer(this.ruleLayers[layerid]));
    this.ruleLayers = {};
    if (trigger.rules && trigger.rules.length) {
      trigger.rules.forEach((rule) => {
        if (rule.comparator === '$geowithin') {
          this.addRule(rule);
        }
      });
      this.map.getView().fit(this.allRuleSource.getExtent(), this.map.getSize());
    }
  }

  addRule(rule) {
    if (isEmpty(rule.rhs)) return;
    const ruleSource = new ol.source.Vector();
    const features = format.readFeatures(rule.rhs);
    features.forEach((feature) => {
      feature.getGeometry().transform('EPSG:4326', 'EPSG:3857');
      ruleSource.addFeature(feature);
      this.allRuleSource.addFeature(feature);
    });
    const layer = new ol.layer.Vector({
      source: ruleSource,
      style: triggerStyle,
    });
    this.ruleLayers[rule.id] = layer;
    this.map.addLayer(layer);
    this.setState(prevState => ({
      activeRules: {
        ...prevState.activeRules,
        [rule.id]: true,
      },
    }));
  }

  toggleRule(rule) {
    if (this.ruleLayers[rule.id]) {
      const layer = this.ruleLayers[rule.id];
      const active = this.state.activeRules[rule.id];
      if (active) {
        this.map.removeLayer(layer);
      } else {
        this.map.addLayer(layer);
      }
      this.setState(prevState => ({
        activeRules: {
          ...prevState.activeRules,
          [rule.id]: !active,
        },
      }));
    }
  }

  viewRule(rule) {
    if (this.ruleLayers[rule.id]) {
      const layer = this.ruleLayers[rule.id];
      const fs = layer.getSource().getFeatures();
      this.map.getView().fit(layer.getSource().getExtent(), this.map.getSize());
      this.select.getFeatures().clear();
      fs.forEach(f => this.select.getFeatures().push(f));
    }
  }

  renderRules() {
    const ruleList = this.props.trigger.rules.length === 0 ?
      <span className="note">No rules have been added to this trigger.</span> :
      this.props.trigger.rules.map(rule => (
        <div className="form-item mini">
          <div className="properties">
            <PropertyListItem name={'Type'} value={rule.comparator.replace('$', '')} />
          </div>
          {this.state.editingRule === rule.id ?
            <div className="btn-toolbar plain">
              <span className="btn-plain" onClick={() => this.onSaveRule(rule)}>Save</span>
              <span className="btn-plain" onClick={() => this.onCancelRule(rule)}>
                Cancel
              </span>
            </div> :
            <div className="btn-toolbar plain">
              <span className="btn-plain" onClick={() => this.viewRule(rule)}>View</span>
              <span className="btn-plain" onClick={() => this.onEditRule(rule)}>Edit</span>
              <span className="btn-plain" onClick={() => this.onDeleteRule(rule)}>
                Delete
              </span>
            </div>
          }
        </div>
      ));
    return (<div>
      <h4>Rules</h4>
      <div className="rule-list">
        {ruleList}
      </div>
      {!this.state.creating &&
      <div className="btn-toolbar">
        <button className="btn btn-sc" onClick={this.onAddRule}>Add Rule</button>
      </div>}
    </div>);
  }

  renderEditing() {
    return (
      <div>
        <div className="btn-toolbar">
          <button className="btn btn-sc" onClick={this.onEditTrigger}>Edit Trigger</button>
          <button className="btn btn-danger" onClick={this.onDelete}>Delete</button>
        </div>
      </div>);
  }

  renderCreating() {
    const uploading = this.state.uploadedFile ? <span>{this.state.uploadedFile}</span> :
      (<div>
        <Dropzone
          onDrop={this.onDrop} multiple={false}
          className="drop-zone" activeClassName="drop-zone-active"
        >
          <div>
            <span>Drop file here, or click to select file to upload.</span>
            <br /><br />
            <span>GeoJSON files accepted.</span>
          </div>
        </Dropzone>
        {!!this.state.uploadErr &&
          <p>{this.state.uploadErr}</p>
        }
      </div>);
    const done = (<div className="btn-toolbar">
      <button className="btn btn-sc" onClick={this.onSave}>Save</button>
      <button className="btn btn-sc" onClick={this.onCancel}>Cancel</button>
    </div>);
    if (this.state.creating) {
      return (<div className="add-rule">
        <h4>Add Rule</h4>
        <div className="form-group">
          <label htmlFor="comparator" >Rule Type:</label>
          <select
            id="comparator" className="form-control"
            value={this.state.rule_comparator}
            onChange={this.onRuleComparatorChange}
          >
            <option value="$geowithin">geowithin</option>
          </select>
        </div>
        {this.state.drawing && done}
        {this.state.uploading && <div>{uploading}{done}</div>}
        {!this.state.drawing && !this.state.uploading &&
        <div>
          <div className="btn-toolbar">
            <button className="btn btn-sc" onClick={this.onDraw}>Draw</button>
            <button className="btn btn-sc" onClick={this.onUpload}>Upload</button>
          </div>
          <div className="btn-toolbar">
            <button className="btn btn-default" onClick={this.onCancel}>Cancel</button>
          </div>
          </div>}
      </div>);
    }
    return null;
  }

  render() {
    const { trigger } = this.props;
    if (this.state.editingTrigger) {
      return (
        <div className="wrapper">
          <section className="main">
            <TriggerForm
              trigger={trigger}
              cancel={this.onCancelEditTrigger}
              onSave={this.onEditTriggerSave}
              errors={this.props.errors}
              actions={this.props.actions}
              stores={this.props.stores}
            />
          </section>
        </div>
      );
    }
    return (
      <div className="trigger-details">
        <div className="trigger-props">
          <TriggerItem trigger={trigger} stores={this.props.stores} />
          {this.renderEditing()}
          {this.renderRules()}
          {this.renderCreating()}
        </div>
        <div className="trigger-map" ref={(c) => { this.mapRef = c; }} />
      </div>
    );
  }
}

TriggerDetails.propTypes = {
  trigger: PropTypes.object.isRequired,
  menu: PropTypes.object.isRequired,
  actions: PropTypes.object.isRequired,
  stores: PropTypes.object.isRequired,
  errors: PropTypes.object.isRequired,
};

export default TriggerDetails;
