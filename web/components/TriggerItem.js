import React, { PropTypes } from 'react';
import { Link } from 'react-router';
import PropertyListItem from './PropertyListItem';

const TriggerItem = ({ trigger, stores }) => (
  <div className="form-item">
    <h4><Link to={`/triggers/${trigger.id}`}>{trigger.name}</Link></h4>
    <p>{trigger.description}</p>
    <p>{trigger.rules ?
      (trigger.rules.length + (trigger.rules.length === 1 ? ' rule' : ' rules'))
       : '0 rules'}
    </p>
    <div className="properties">
      {!!trigger.recipients.emails.length &&
        <PropertyListItem
          name={'Recipient Emails'}
          value={`\n${trigger.recipients.emails.join('\n')}`}
        />
      }
      {!!trigger.recipients.devices.length &&
        <PropertyListItem
          name={'Recipient Devices'}
          value={trigger.recipients.devices.join(', ')}
        />
      }
      {!!trigger.stores.length &&
        <PropertyListItem
          name={'Source Stores'}
          value={trigger.stores.map((id) => {
            if (stores[id]) return stores[id].name;
            return false;
          }).filter(n => n).join(', ')}
        />
      }
      <PropertyListItem name={'Repeated'} value={trigger.repeated ? 'Always' : 'Once'} />
    </div>
  </div>
);

TriggerItem.propTypes = {
  trigger: PropTypes.object.isRequired,
  stores: PropTypes.object.isRequired,
};

export default TriggerItem;
