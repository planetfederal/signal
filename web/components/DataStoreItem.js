import React, { PropTypes } from 'react';
import { Link } from 'react-router';
import PropertyListItem from './PropertyListItem';
import '../style/FormList.less';

const DataStoreItem = ({ store }) => (
  <div className="form-item">
    <h4><Link to={`/stores/${store.id}`}>{store.name}</Link></h4>
    <div className="properties">
      <PropertyListItem name={'ID'} value={store.id} />
      <PropertyListItem name={'Type'} value={store.store_type} />
      <PropertyListItem name={'URI'} value={store.uri} />
      <PropertyListItem name={'Version'} value={store.version} />
      <PropertyListItem name={'Team'} value={store.team_name} />
      {(store.options && store.options.polling) &&
      <PropertyListItem name={'Polling Interval'} value={store.options.polling} />}
      {store.default_layers && store.default_layers.length ?
        <PropertyListItem name={'Default Layers'} value={store.default_layers.join(', ')} />
        : '' }
    </div>
  </div>
);

DataStoreItem.propTypes = {
  store: PropTypes.object.isRequired,
};

export default DataStoreItem;
