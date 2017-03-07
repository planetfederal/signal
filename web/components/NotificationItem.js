import React, { PropTypes } from 'react';
import format from 'date-fns/format';
import PropertyListItem from './PropertyListItem';
import '../style/FormList.less';

const dateFormat = 'dddd, MMMM Do YYYY, h:mm:ss a';

const NotificationItem = ({ notification }) => (
  <div className="form-item">
    <div className="properties">
      <PropertyListItem name={'Recipient'} value={notification.recipient} />
      <PropertyListItem name={'Type'} value={notification.type} />
      <PropertyListItem
        name={'Sent'}
        value={notification.sent ? format(notification.sent, dateFormat)
         : 'Not Sent'}
      />
      <PropertyListItem
        name={'Delivered'}
        value={notification.delivered ? format(notification.delivered, dateFormat)
          : 'Not Delivered'}
      />
    </div>
  </div>
);

NotificationItem.propTypes = {
  notification: PropTypes.object.isRequired,
};

export default NotificationItem;
