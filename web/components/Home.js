import React, { PropTypes } from 'react';
import PropertyListItem from './PropertyListItem';

const Home = ({ stores, spatial_triggers }) => (
  <div className="wrapper">
    <section className="main">
      <p>Welcome to the signal dashboard.</p>
      <div className="form-list">
        <div className="form-item">
          <div className="properties">
            <PropertyListItem name={'Stores'} value={Object.keys(stores).length} />
            <PropertyListItem
              name={'Spatial Triggers'} value={Object.keys(spatial_triggers).length}
            />
          </div>
        </div>
      </div>
    </section>
  </div>
);

Home.propTypes = {
  stores: PropTypes.object.isRequired,
  spatial_triggers: PropTypes.object.isRequired,
};

export default Home;
