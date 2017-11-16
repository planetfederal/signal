import React, { PropTypes } from "react";
import NotificationItem from "./NotificationItem";
import "../style/FormList.less";

const NotificationList = ({ notifications }) => (
  <div className="form-list">
    {notifications.map(n => <NotificationItem key={n.id} notification={n} />)}
  </div>
);

export default NotificationList;
