import React, { Component, PropTypes } from 'react';
import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import * as processorActions from '../ducks/processors';
import { ProcessorForm } from '../components/ProcessorForm';
import ProcessorList from '../components/ProcessorList';

const emptyProcessor = {
  name: '',
  description: '',
  repeated: false,
  recipients: {
    devices: [],
    emails: [],
  },
  rules: [],
};

class ProcessorsContainer extends Component {

  constructor(props) {
    super(props);
    this.state = {
      adding: false,
    };

    this.add = this.add.bind(this);
    this.cancel = this.cancel.bind(this);
    this.create = this.create.bind(this);
  }

  componentDidMount() {
    this.props.actions.loadProcessors();
  }

  add() {
    this.setState({ adding: !this.state.adding });
  }

  cancel() {
    this.setState({ adding: false });
  }

  create(processor) {
    this.setState({ adding: false });
    this.props.actions.addProcessor(processor);
  }

  render() {
    const { children } = this.props;
    if (children) {
      return (
        <div className="wrapper">
          {children}
        </div>
      );
    }
    return (
      <div className="wrapper">
        <section className="main">
          {this.state.adding ?
            <ProcessorForm
              processor={emptyProcessor}
              cancel={this.cancel}
              onSave={this.create}
              errors={this.props.errors}
              actions={this.props.actions}
            /> :
            <div className="btn-toolbar">
              <button className="btn btn-sc" onClick={this.add}>Create Processor</button>
            </div>}
          <ProcessorList {...this.props} />
        </section>
      </div>
    );
  }
}

ProcessorsContainer.propTypes = {
  errors: PropTypes.object.isRequired,
  actions: PropTypes.object.isRequired,
  children: PropTypes.object,
};

const mapStateToProps = state => ({
  spatial_processors: state.sc.processors.spatial_processors,
  errors: state.sc.processors.errors,
});

const mapDispatchToProps = dispatch => ({
  actions: bindActionCreators(processorActions, dispatch),
});

export default connect(mapStateToProps, mapDispatchToProps)(ProcessorsContainer);
