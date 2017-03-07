/* global describe, it, before, after */
import expect from 'expect';
import configureMockStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import deepFreeze from 'deep-freeze';
import sinon from 'sinon';
import { API_URL } from 'config';
import * as dataStores from '../ducks/dataStores';
import reducer from '../ducks/dataStores';
import mockDataStores from './data/mockDataStores';

// test the action creators that return an object
describe('dataStores action creators', () => {
  it('should create an action that receives dataStores', () => {
    const expectedAction = {
      type: dataStores.LOAD_SUCCESS,
      stores: mockDataStores,
    };
    expect(dataStores.receiveStores(mockDataStores)).toEqual(expectedAction);
  });
});
//
// test the async action creators that return functions
const middlewares = [thunk];
const mockStore = configureMockStore(middlewares);

describe('dataStores async action creators', () => {
  before(() => {
    this.server = sinon.fakeServer.create();
    this.server.autoRespond = true;
    this.server.respondWith('GET', `${API_URL}stores`,
      [200, { 'Content-Type': 'application/json' }, JSON.stringify(mockDataStores)],
    );
    this.server.respondWith('POST', `${API_URL}stores`,
      [200, { 'Content-Type': 'application/json' }, JSON.stringify([mockDataStores[0]])],
    );
    this.server.respondWith('PUT', `${API_URL}stores/1`,
      [200, { 'Content-Type': 'application/json' }, JSON.stringify([mockDataStores[0]])],
    );
  });

  after(() => {
    this.server.restore();
  });

  it('should emit a LOAD_SUCCESS after stores are fetch successfully', (done) => {
    const expectedActions = [
      { type: dataStores.LOAD },
      { type: dataStores.LOAD_SUCCESS, stores: mockDataStores },
    ];
    const store = mockStore({ stores: mockDataStores });
    store.dispatch(dataStores.loadDataStores())
      .then(() => {
        expect(store.getActions()).toEqual(expectedActions);
      }).then(done).catch(done);
  });

  it('should emit a LOAD_SUCCESS after a new store is added', (done) => {
    const expectedActions = [
      { type: dataStores.LOAD },
      { type: dataStores.LOAD_SUCCESS, stores: [mockDataStores[0]] },
    ];
    const store = mockStore({ stores: [] });
    store.dispatch(dataStores.submitNewDataStore(mockDataStores[0]))
      .then(() => {
        expect(store.getActions()).toEqual(expectedActions);
      }).then(done).catch(done);
  });

  it('should emit a LOAD_SUCCESS after a store is updated', (done) => {
    const expectedActions = [
      { type: dataStores.LOAD },
      { type: dataStores.LOAD_SUCCESS, stores: [mockDataStores[0]] },
    ];
    const store = mockStore({ stores: [] });
    store.dispatch(dataStores.updateDataStore(1, mockDataStores[0]))
      .then(() => {
        expect(store.getActions()).toEqual(expectedActions);
      }).then(done).catch(done);
  });

  it('should emit a LOAD_SUCCESS after multiple stores are updated', (done) => {
    const expectedActions = [
      { type: dataStores.LOAD },
      { type: dataStores.LOAD_SUCCESS, stores: mockDataStores },
    ];
    const store = mockStore({ stores: [] });
    store.dispatch(dataStores.updateDataStores(mockDataStores))
      .then(() => {
        expect(store.getActions()).toEqual(expectedActions);
      }).then(done).catch(done);
  });
});

// test that the reducers return the correct state after applying the action
describe('dataStores reducer', () => {
  const initialState = {
    loading: false,
    loaded: false,
    stores: [],
    addingNewDataStore: false,
    newDataStoreId: null,
  };

  // ensure reducer does not mutate initialState
  deepFreeze(initialState);

  it('should return the initial state', () => {
    expect(reducer(undefined, {})).toEqual(initialState);
  });

  it('should handle LOAD', () => {
    expect(reducer(initialState, { type: dataStores.LOAD })).toEqual({
      loading: true,
      loaded: false,
      stores: [],
      addingNewDataStore: false,
      newDataStoreId: null,
    });
  });

  it('should handle LOAD_SUCCESS', () => {
    expect(
      reducer(initialState, {
        type: dataStores.LOAD_SUCCESS,
        stores: mockDataStores,
      }),
    ).toEqual({
      loading: false,
      loaded: true,
      stores: mockDataStores,
      addingNewDataStore: false,
      newDataStoreId: null,
    });
  });

  it('should handle LOAD_FAIL', () => {
    const state = reducer(initialState, {
      type: dataStores.LOAD_FAIL,
      error: 'load fail',
    });
    expect(state.loading).toEqual(false);
    expect(state.loaded).toEqual(false);
    expect(state.error).toExist();
  });
});
