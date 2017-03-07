/* global describe, it, before, after */
import React from 'react';
import expect from 'expect';
import { shallow } from 'enzyme';
import DataStoresList from '../../components/DataStoresList';
import DataStoreItem from '../../components/DataStoreItem';
import dataStores from '../data/mockDataStores';

describe('DataStoresList', () => {
  function setup(props = {}) {
    const defaultProps = {
      dataStores: [],
      onSubmit: () => {},
    };
    const newProps = Object.assign(defaultProps, props);

    const component = shallow(<DataStoresList {...newProps} />);

    return { component };
  }

  it('should render correctly', () => {
    const { component } = setup();
    expect(component.type()).toBe('div');
  });

  it('should render 1 data store list when passed 1 store in the props', () => {
    const { component } = setup({ dataStores });
    expect(component.find(DataStoreItem).length).toBe(1);
  });

  it('should set the dataStore prop of the data store', () => {
    const { component } = setup({ dataStores });
    expect(component.find(DataStoreItem).at(0).props().store).toBe(dataStores[0]);
  });
});
