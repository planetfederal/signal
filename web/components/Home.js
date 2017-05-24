import React, { PropTypes } from 'react';
import PropertyListItem from './PropertyListItem';

const Home = ({ spatial_processors }) => (
  <div className="wrapper">
    <section className="main">
      <p>Welcome to the signal dashboard.</p>
      <div className="form-list">
        <div className="form-item">
          <div className="properties">
            <PropertyListItem
              name={'Spatial Processors'} value={Object.keys(spatial_processors).length}
            />
          </div>
        </div>
      </div>
    </section>
  </div>
);

Home.propTypes = {
  spatial_processors: PropTypes.object.isRequired,
};

export default Home;
