import * as request from 'superagent-bluebird-promise';
import { API_URL } from 'config';
import { push } from 'react-router-redux';
import keyBy from 'lodash/keyBy';
import { initStore } from '../utils';

// define action types
export const LOAD = 'sc/dataStores/LOAD';
export const LOAD_STORES = 'sc/dataStores/LOAD_STORES';
export const LOAD_STORE = 'sc/dataStores/LOAD_STORE';
export const LOAD_FAIL = 'sc/dataStores/LOAD_FAIL';
export const STORE_ERRORS = 'sc/dataStores/STORE_ERRORS';
export const STORE_ERROR = 'sc/dataStores/STORE_ERROR';
export const WFS_LAYER_LIST = 'sc/dataStores/WFS_LAYER_LIST';

// define an initialState
const initialState = {
  loading: false,
  loaded: false,
  stores: {},
  addingNewDataStore: false,
  newDataStoreId: null,
  storeErrors: {},
  layerList: [],
};

// export the reducer function, (previousState, action) => newState
export default function reducer(state = initialState, action = {}) {
  switch (action.type) {
    case LOAD:
      return {
        ...state,
        loading: true,
        addingNewDataStore: false,
      };
    case LOAD_STORES:
      return {
        ...state,
        loading: false,
        loaded: true,
        stores: action.payload.stores,
      };
    case LOAD_STORE:
      return {
        ...state,
        loading: false,
        loaded: true,
        stores: {
          ...state.stores,
          [action.payload.store.id]: action.payload.stores,
        },
      };
    case LOAD_FAIL:
      return {
        ...state,
        loading: false,
        loaded: false,
        error: action.payload.error,
      };
    case STORE_ERRORS:
      return {
        ...state,
        storeErrors: action.payload.errors,
      };
    case STORE_ERROR:
      return {
        ...state,
        storeErrors: {
          ...state.storeErrors,
          [action.payload.field]: action.payload.error,
        },
      };
    case WFS_LAYER_LIST:
      return {
        ...state,
        layerList: action.payload.layerList,
      };
    default: return state;
  }
}

// export the action creators (functions that return actions or functions)
export function receiveStores(stores) {
  return (dispatch, getState) => {
    const { sc } = getState();
    dispatch({
      type: LOAD_STORES,
      payload: { stores: keyBy(stores.map(initStore(sc.auth.user.teams)), 'id') },
    });
  };
}

export function receiveStore(store) {
  return (dispatch, getState) => {
    const { sc } = getState();
    dispatch({
      type: LOAD_STORE,
      payload: { store: initStore(sc.auth.user.teams)(store) },
    });
  };
}

export function loadDataStore(storeId) {
  return (dispatch, getState) => {
    const { sc } = getState();
    const token = sc.auth.token;
    dispatch({ type: LOAD });
    return request
      .get(`${API_URL}stores/${storeId}`)
      .set('Authorization', `Token ${token}`)
      .then(
        res => dispatch(receiveStore(res.body.result)),
        error => dispatch({ type: LOAD_FAIL, error }),
      );
  };
}

export function loadDataStores() {
  return (dispatch, getState) => {
    const { sc } = getState();
    const token = sc.auth.token;
    dispatch({ type: LOAD });
    return request
      .get(`${API_URL}stores`)
      .set('Authorization', `Token ${token}`)
      .then(
        res => dispatch(receiveStores(res.body.result)),
        error => dispatch({ type: LOAD_FAIL, error }),
      );
  };
}

export function submitNewDataStore(store) {
  return (dispatch, getState) => {
    const { sc } = getState();
    const token = sc.auth.token;
    const newStore = {
      ...store,
      team_id: sc.auth.selectedTeamId,
    };
    return request
      .post(`${API_URL}stores`)
      .set('Authorization', `Token ${token}`)
      .send(newStore)
      .then(() => dispatch(loadDataStores()));
  };
}

export function updateDataStore(id, store) {
  return (dispatch, getState) => {
    const { sc } = getState();
    const token = sc.auth.token;
    const newStore = {
      ...store,
      team_id: sc.auth.selectedTeamId,
    };
    return request
      .put(`${API_URL}stores/${id}`)
      .set('Authorization', `Token ${token}`)
      .send(newStore)
      .then(() => dispatch(loadDataStores()));
  };
}

export function updateDataStores(values) {
  return (dispatch, getState) => {
    const { sc } = getState();
    const token = sc.auth.token;
    return Promise.map(values, value => request
        .put(`${API_URL}stores/${value.id}`)
        .set('Authorization', `Token ${token}`)
        .send(value)
        .promise())
        .then(() => dispatch(loadDataStores()));
  };
}

export function deleteStore(storeId) {
  return (dispatch, getState) => {
    const { sc } = getState();
    const token = sc.auth.token;
    return request
      .delete(`${API_URL}stores/${storeId}`)
      .set('Authorization', `Token ${token}`)
      .then(() => {
        dispatch(loadDataStores());
        return dispatch(push('/stores'));
      });
  };
}

export function updateStoreErrors(errors) {
  return {
    type: STORE_ERRORS,
    payload: { errors },
  };
}

export function addStoreError(field, error) {
  return {
    type: STORE_ERROR,
    payload: { field, error },
  };
}

export function updateWFSLayerList(layerList) {
  return {
    type: WFS_LAYER_LIST,
    payload: { layerList },
  };
}

export function getWFSLayers(uri) {
  return (dispatch, getState) => {
    const { sc } = getState();
    const token = sc.auth.token;
    return request
      .get(`${API_URL}wfs/getCapabilities?url=${encodeURIComponent(uri)}`)
      .set('Authorization', `Token ${token}`)
      .then((res) => {
        dispatch(updateWFSLayerList(res.body.result));
        dispatch(addStoreError('default_layers', false));
      }, () => {
        dispatch(updateWFSLayerList([]));
        dispatch(addStoreError('default_layers', 'Could Not Find Layers'));
      });
  };
}
