import React, { Component, PropTypes } from 'react';
import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import { Link } from 'react-router';
import { emptyStore } from '../utils';
import * as dataStoresActions from '../ducks/dataStores';
import DataStoresList from '../components/DataStoresList';
import { DataStoreForm } from '../components/DataStoreForm';

class DataStoresContainer extends Component {

  constructor(props) {
    super(props);
    this.state = {
      addingNewDataStore: false,
    };

    this.addNewDataStore = this.addNewDataStore.bind(this);
    this.addNewDataStoreCancel = this.addNewDataStoreCancel.bind(this);
    this.submitNewDataStore = this.submitNewDataStore.bind(this);
  }

  componentDidMount() {
    this.props.actions.loadDataStores();
  }

  addNewDataStore() {
    this.setState({ addingNewDataStore: true });
  }

  addNewDataStoreCancel() {
    this.setState({ addingNewDataStore: false });
  }

  submitNewDataStore(storeId, data) {
    this.setState({ addingNewDataStore: false });
    this.props.actions.submitNewDataStore(data);
  }

  renderStoreForm() {
    return this.state.addingNewDataStore ?
      <DataStoreForm
        onSubmit={this.submitNewDataStore}
        cancel={this.addNewDataStoreCancel}
        actions={this.props.actions}
        errors={this.props.storeErrors}
        layerList={this.props.layerList}
        store={emptyStore}
      /> :
      <div className="btn-toolbar">
        <button className="btn btn-sc" onClick={this.addNewDataStore}>Create Store</button>
      </div>;
  }

  render() {
    const { loading, stores, children, selectedTeamId } = this.props;
    if (children) {
      return (
        <div className="wrapper">
          <section className="main">
            {children}
          </section>
        </div>
      );
    }
    return (
      <div className="wrapper">
        <section className="main">
          {selectedTeamId ? <div>
            {loading ? <p>Fetching Data Stores...</p> : this.renderStoreForm() }
            <DataStoresList dataStores={stores} selectedTeamId={selectedTeamId} />
          </div> : <p><Link to="/teams">Join</Link> a team to view Stores.</p>}
        </section>
      </div>
    );
  }
}

DataStoresContainer.propTypes = {
  actions: PropTypes.object.isRequired,
  loading: PropTypes.bool.isRequired,
  stores: PropTypes.object.isRequired,
  children: PropTypes.object,
  selectedTeamId: PropTypes.number,
  storeErrors: PropTypes.object,
  layerList: PropTypes.array,
};

const mapStateToProps = state => ({
  loading: state.sc.dataStores.loading,
  stores: state.sc.dataStores.stores,
  storeErrors: state.sc.dataStores.storeErrors,
  layerList: state.sc.dataStores.layerList,
  selectedTeamId: state.sc.auth.selectedTeamId,
});

const mapDispatchToProps = dispatch => ({
  actions: bindActionCreators(dataStoresActions, dispatch),
});

  // connect this "smart" container component to the redux store
export default connect(mapStateToProps, mapDispatchToProps)(DataStoresContainer);
