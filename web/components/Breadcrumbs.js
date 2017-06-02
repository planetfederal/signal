import React, { Component, PropTypes } from 'react';
import { connect } from 'react-redux';
import find from 'lodash/find';
import ReactBreadcrumbs from 'react-breadcrumbs';

class Breadcrumbs extends Component {

  render() {
    const { sc, params } = this.props;
    const routes = this.props.routes.map((route) => {
      if (route.path === '/forms/:form_key') {
        const form = sc.forms.forms[params.form_key];
        if (form) {
          route.name = form.form_label;
        }
      }
      if (route.path === '/processors/:id') {
        const processor = find(sc.processors.spatial_processors, { id: params.id });
        if (processor) {
          route.name = processor.name;
        }
      }
      if (route.path === '/teams/:id') {
        const team = find(sc.teams.teams, { id: +params.id });
        if (team) {
          route.name = team.name;
        }
      }
      if (route.path === '/notifications/:id') {
        route.name = 'Notification';
      }
      return route;
    });

    return (
      <div>
        <ReactBreadcrumbs
          separator="&nbsp;&nbsp;&nbsp;&nbsp;>&nbsp;&nbsp;&nbsp;&nbsp;"
          excludes={['Home']} hideNoPath routes={routes} params={this.props.params}
        />
      </div>
    );
  }
}

Breadcrumbs.propTypes = {
  sc: PropTypes.object.isRequired,
  params: PropTypes.object.isRequired,
  routes: PropTypes.array.isRequired,
};


const mapStateToProps = state => ({
  sc: state.sc,
});

  // connect this "smart" container component to the redux store
export default connect(mapStateToProps)(Breadcrumbs);
