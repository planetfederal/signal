import React, { PropTypes } from 'react';
import TriggerItem from './TriggerItem';
import '../style/FormList.less';

const TriggerList = ({ spatial_triggers, stores }) => (
  <div className="form-list">
    {Object.keys(spatial_triggers).map(k =>
      <TriggerItem
        key={spatial_triggers[k].id}
        trigger={spatial_triggers[k]}
        stores={stores}
      />)}
  </div>
);

TriggerList.propTypes = {
  spatial_triggers: PropTypes.object.isRequired,
  stores: PropTypes.object.isRequired,
};

export default TriggerList;
