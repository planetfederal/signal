/* global describe, it, before, after */
import React from 'react';
import expect from 'expect';
import { shallow } from 'enzyme';
import { DataStoreForm, validate } from '../../components/DataStoreForm';
import mockDataStores from '../data/mockDataStores';

describe('DataStoreForm', () => {
  function setup(props = {}) {
    const defaultProps = {
      store: mockDataStores[0],
      onSubmit: () => {},
      cancel: () => {},
    };
    const newProps = Object.assign(defaultProps, props);

    const component = shallow(<DataStoreForm {...newProps} />);

    return { component };
  }

  it('should render correctly', () => {
    const { component } = setup();
    expect(component.type()).toBe('div');
    expect(component.find('input').length).toBe(3);
    expect(component.find('select').length).toBe(1);
  });

  it('should validate', () => {
    expect(validate(mockDataStores[0])).toEqual({});
  });

  it('should return errors on validate', () => {
    expect(validate({})).toEqual({
      name: 'Required',
      store_type: 'Required',
      version: 'Required',
    });
  });
});
