import React, { Component, PropTypes } from 'react';
import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import find from 'lodash/find';
import TriggerDetails from '../components/TriggerDetails';
import * as triggerActions from '../ducks/triggers';
import * as storeActions from '../ducks/dataStores';

class TriggerDetailsContainer extends Component {

  componentDidMount() {
    if (!this.props.trigger) {
      this.props.actions.loadTrigger(this.props.id);
      this.props.storeActions.loadDataStores();
    }
  }

  render() {
    return (
      <section className="main noPad">
        {this.props.trigger ? <TriggerDetails {...this.props} /> : null}
      </section>
    );
  }
}

TriggerDetailsContainer.propTypes = {
  actions: PropTypes.object.isRequired,
  storeActions: PropTypes.object.isRequired,
  trigger: PropTypes.object,
  id: PropTypes.string.isRequired,
};

const mapStateToProps = (state, ownProps) => ({
  id: ownProps.params.id,
  trigger: find(state.sc.triggers.spatial_triggers, { id: ownProps.params.id }),
  menu: state.sc.menu,
  stores: state.sc.dataStores.stores,
  errors: state.sc.triggers.errors,
});

const mapDispatchToProps = dispatch => ({
  actions: bindActionCreators(triggerActions, dispatch),
  storeActions: bindActionCreators(storeActions, dispatch),
});

  // connect this "smart" container component to the redux store
export default connect(mapStateToProps, mapDispatchToProps)(TriggerDetailsContainer);
