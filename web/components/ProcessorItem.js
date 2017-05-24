import React, { PropTypes } from 'react';
import { Link } from 'react-router';
import PropertyListItem from './PropertyListItem';

const ProcessorItem = ({ processor }) => (
  <div className="form-item">
    <h4><Link to={`/processors/${processor.id}`}>{processor.name}</Link></h4>
    <p>{processor.description}</p>
    <p>{processor.rules ?
      (processor.rules.length + (processor.rules.length === 1 ? ' rule' : ' rules'))
       : '0 rules'}
    </p>
    <div className="properties">
      {!!processor.recipients.emails.length &&
        <PropertyListItem
          name={'Recipient Emails'}
          value={`\n${processor.recipients.emails.join('\n')}`}
        />
      }
      {!!processor.recipients.devices.length &&
        <PropertyListItem
          name={'Recipient Devices'}
          value={processor.recipients.devices.join(', ')}
        />
      }
      <PropertyListItem name={'Repeated'} value={processor.repeated ? 'Always' : 'Once'} />
    </div>
  </div>
);

ProcessorItem.propTypes = {
  processor: PropTypes.object.isRequired,
};

export default ProcessorItem;
