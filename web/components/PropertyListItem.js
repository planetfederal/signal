import React, { PropTypes } from 'react';

const PropertyListItem = ({ name, value }) => (
  <div className="property">
    <div className="name">{`${name}: `}</div>
    <div className="value">{value}</div>
  </div>
);

PropertyListItem.propTypes = {
  name: PropTypes.string.isRequired,
  value: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.number,
  ]),
};

export default PropertyListItem;
