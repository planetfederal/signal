import React, { PropTypes } from 'react';
import { Link } from 'react-router';
import Breadcrumbs from './Breadcrumbs';

const Header = props => (
  <header>
    <div className="header-title">
      <span className="menu" onClick={props.toggleMenu}>&#9776;</span>
      <Link to="/">signal</Link>
    </div>
    {props.isAuthenticated &&
      <nav>
        <Breadcrumbs {...props} />
      </nav>
    }
  </header>
);

Header.propTypes = {
  isAuthenticated: PropTypes.bool.isRequired,
  toggleMenu: PropTypes.func.isRequired,
};

export default Header;
