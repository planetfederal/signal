import React, { PropTypes } from 'react';
import { Link } from 'react-router';

const SideMenuItem = ({ path, name, onClick }) => (
  <div className="side-menu-item">
    <Link to={path} activeClassName="active" onClick={onClick}>{name}</Link>
  </div>
);

SideMenuItem.propTypes = {
  path: PropTypes.string.isRequired,
  name: PropTypes.string.isRequired,
  onClick: PropTypes.func.isRequired,
};

const SideMenu = ({ isAuthenticated, actions, userName,
  closeMenu, menuOpen }) => (
    <div className={`side-menu ${menuOpen ? 'open' : 'closed'}`}>
      {isAuthenticated ?
        <nav>
          <SideMenuItem path={'/processors'} name={'Processors'} onClick={closeMenu} />
          <div className="side-menu-separator" />
          <SideMenuItem path={'/user'} name={userName} onClick={closeMenu} />
          <SideMenuItem
            path={'/inputs'} name={'Inputs'}
            onClick={closeMenu}
          />
          <div className="side-menu-separator" />
          <SideMenuItem
            path={'/outputs'} name={'Outputs'}
            onClick={closeMenu}
          />
          <div className="side-menu-separator" />
          <SideMenuItem
            path={'/mappings'} name={'Maps'}
            onClick={closeMenu}
          />
          <div className="side-menu-separator" />
          <SideMenuItem
            path={'/filters'} name={'Filters'}
            onClick={closeMenu}
          />
          <div className="side-menu-separator" />
          <SideMenuItem
            path={'/reducers'} name={'Reducers'}
            onClick={closeMenu}
          />


          <SideMenuItem
            path={'/login'} name={'Sign Out'} onClick={() => {
              actions.logoutAndRedirect();
              closeMenu();
            }}
          />
          <div className="side-menu-separator" />
          <div className="side-menu-spacer" />
          <div className="side-menu-item bottom">
            <span>{`v${VERSION}`}</span>
          </div>
        </nav>
        : <nav>
          <SideMenuItem path={'/login'} name={'Sign In'} onClick={closeMenu} />
          <SideMenuItem path={'/signup'} name={'Sign Up'} onClick={closeMenu} />
        </nav>
      }
    </div>
);

SideMenu.propTypes = {
  isAuthenticated: PropTypes.bool.isRequired,
  userName: PropTypes.string,
  actions: PropTypes.object.isRequired,
  closeMenu: PropTypes.func.isRequired,
  menuOpen: PropTypes.bool.isRequired,
};

export default SideMenu;
