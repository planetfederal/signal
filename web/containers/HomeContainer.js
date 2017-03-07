import React, { Component, PropTypes } from 'react';
import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import * as storeActions from '../ducks/dataStores';
import * as triggerActions from '../ducks/triggers';
import Home from '../components/Home';

class HomeContainer extends Component {
  componentDidMount() {
    this.props.triggerActions.loadTriggers();
    this.props.storeActions.loadDataStores();
  }
  render() {
    return (
      <Home {...this.props} />
    );
  }
}

HomeContainer.propTypes = {
  triggerActions: PropTypes.object.isRequired,
  storeActions: PropTypes.object.isRequired,
};

const mapStateToProps = state => ({
  stores: state.sc.dataStores.stores,
  spatial_triggers: state.sc.triggers.spatial_triggers,
});

const mapDispatchToProps = dispatch => ({
  triggerActions: bindActionCreators(triggerActions, dispatch),
  storeActions: bindActionCreators(storeActions, dispatch),
});

export default connect(mapStateToProps, mapDispatchToProps)(HomeContainer);
