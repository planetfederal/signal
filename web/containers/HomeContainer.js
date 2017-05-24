import React, { Component, PropTypes } from 'react';
import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import * as processorActions from '../ducks/processors';
import Home from '../components/Home';

class HomeContainer extends Component {
  componentDidMount() {
    this.props.processorActions.loadProcessors();
  }
  render() {
    return (
      <Home {...this.props} />
    );
  }
}

HomeContainer.propTypes = {
  processorActions: PropTypes.object.isRequired,
};

const mapStateToProps = state => ({
  spatial_processors: state.sc.processors.spatial_processors,
});

const mapDispatchToProps = dispatch => ({
  processorActions: bindActionCreators(processorActions, dispatch),
});

export default connect(mapStateToProps, mapDispatchToProps)(HomeContainer);
