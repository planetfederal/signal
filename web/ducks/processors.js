import * as request from 'superagent-bluebird-promise';
import keyBy from 'lodash/keyBy';
import omit from 'lodash/omit';
import { push } from 'react-router-redux';
import { API_URL } from 'config';

export const LOAD_SPATIAL_PROCESSORS = 'sc/processors/LOAD_SPATIAL_PROCESSORS';
export const ADD_PROCESSOR = 'sc/processors/ADD_PROCESSOR';
export const UPDATE_PROCESSOR = 'sc/processors/UPDATE_PROCESSOR';
export const DELETE_PROCESSOR = 'sc/processors/DELETE_PROCESSOR';
export const PROCESSOR_ERRORS = 'sc/processors/PROCESSOR_ERRORS';

const initialState = {
  spatial_processors: {},
  errors: {},
};

export default function reducer(state = initialState, action = {}) {
  switch (action.type) {
    case LOAD_SPATIAL_PROCESSORS:
      return {
        ...state,
        spatial_processors: action.payload.spatial_processors,
      };
    case ADD_PROCESSOR:
      return {
        ...state,
        spatial_processors: {
          ...state.spatial_processors,
          [action.payload.processor.id]: action.payload.processor,
        },
      };
    case UPDATE_PROCESSOR:
      return {
        ...state,
        spatial_processors: {
          ...state.spatial_processors,
          [action.payload.processor.id]: action.payload.processor,
        },
      };
    case DELETE_PROCESSOR:
      return {
        ...state,
        spatial_processors: omit(state.spatial_processors, action.payload.processor.id),
      };
    case PROCESSOR_ERRORS:
      return {
        ...state,
        errors: action.payload.errors,
      };
    default: return state;
  }
}

export function updateProcessorErrors(errors) {
  return {
    type: PROCESSOR_ERRORS,
    payload: {
      errors,
    },
  };
}

export function updateProcessor(processor) {
  return (dispatch, getState) => {
    const { sc } = getState();
    const token = sc.auth.token;
    return request
      .put(`${API_URL}processors/${processor.id}`)
      .set('Authorization', `Token ${token}`)
      .send(processor)
      .then(
        () => dispatch(loadProcessor(processor.id, true)),
        err => dispatch(updateProcessorErrors(err.body.error)),
      );
  };
}

export function addProcessor(processor) {
  return (dispatch, getState) => {
    const { sc } = getState();
    const token = sc.auth.token;
    return request
      .post(`${API_URL}processors`)
      .set('Authorization', `Token ${token}`)
      .send(processor)
      .then(
        () => dispatch(loadProcessors()),
        err => dispatch(updateProcessorErrors(err.body.error)),
      );
  };
}

export function receiveProcessors(processors) {
  return {
    type: LOAD_SPATIAL_PROCESSORS,
    payload: {
      spatial_processors: keyBy(processors, 'id'),
    },
  };
}

export function receiveProcessor(processor) {
  return {
    type: ADD_PROCESSOR,
    payload: { processor },
  };
}

export function deleteProcessor(processor) {
  return (dispatch, getState) => {
    const { sc } = getState();
    const token = sc.auth.token;
    return request
      .delete(`${API_URL}processors/${processor.id}`)
      .set('Authorization', `Token ${token}`)
      .then(() => {
        dispatch({
          type: DELETE_PROCESSOR,
          payload: { processor },
        });
        dispatch(push('/processors'));
      });
  };
}


export function loadProcessor(processorId) {
  return (dispatch, getState) => {
    const { sc } = getState();
    const token = sc.auth.token;
    return request
      .get(`${API_URL}processors/${processorId}`)
      .set('Authorization', `Token ${token}`)
      .then(res => res.body.result)
      .then(data => dispatch(receiveProcessor(data)));
  };
}

export function loadProcessors() {
  return (dispatch, getState) => {
    const { sc } = getState();
    const token = sc.auth.token;
    return request
      .get(`${API_URL}processors`)
      .set('Authorization', `Token ${token}`)
      .then(res => res.body.result)
      .then(data => dispatch(receiveProcessors(data)));
  };
}
