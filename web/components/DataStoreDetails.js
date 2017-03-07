import React, { PropTypes } from 'react';
import DataStoreItem from './DataStoreItem';

const DataStoreDetails = ({ store, deleteStore, editStore }) => (
  <div className="store-details">
    <DataStoreItem store={store} />
    <div className="btn-toolbar">
      <button className="btn btn-sc" onClick={() => { editStore(store.id); }}>
        Edit Store
      </button>
      <button className="btn btn-danger" onClick={() => { deleteStore(store.id); }}>
        Delete Store
      </button>
    </div>
  </div>
);

DataStoreDetails.propTypes = {
  store: PropTypes.object.isRequired,
  editStore: PropTypes.func.isRequired,
  deleteStore: PropTypes.func.isRequired,
};

export default DataStoreDetails;
