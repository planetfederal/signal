import React, { PropTypes } from "react";
import { Link } from "react-router";
import format from "date-fns/format";
import PropertyListItem from "./PropertyListItem";
import "../style/FormList.less";

const dateFormat = "dddd, MMMM Do YYYY, h:mm:ss a";

const NotificationItem = ({ notification }) => (
  <div className="form-item">
    <div className="properties">
      <h4>
        <Link to={`/notifications/${notification.id}`}>
          {notification.info.title} - {notification.id}
        </Link>
      </h4>
      <PropertyListItem name={"Recipient"} value={notification.recipient} />
      <PropertyListItem name={"Type"} value={notification.type} />
      <PropertyListItem
        name={"Sent"}
        value={
          notification.sent ? format(notification.sent, dateFormat) : "Not Sent"
        }
      />
    </div>
  </div>
);

NotificationItem.propTypes = {
  notification: PropTypes.object.isRequired
};

export default NotificationItem;
