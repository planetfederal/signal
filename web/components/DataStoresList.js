import React, { PropTypes } from 'react';
import DataStoreItem from './DataStoreItem';
import '../style/FormList.less';

const DataStoresList = ({ dataStores }) => (
  <div className="form-list">
    {Object.keys(dataStores).map((k) => {
      const s = dataStores[k];
      return <DataStoreItem store={s} key={s.id} />;
    })}
  </div>
);

DataStoresList.propTypes = {
  dataStores: PropTypes.object.isRequired,
};

export default DataStoresList;
