/* global describe, it, before, after */
import React from 'react';
import expect from 'expect';
import { shallow } from 'enzyme';
import DataStoreItem from '../../components/DataStoreItem';

describe('DataStoreItem', () => {
  function setup(props = {}) {
    const defaultProps = {
      store: {},
      onSubmit: () => {},
    };
    const newProps = Object.assign(defaultProps, props);

    const component = shallow(<DataStoreItem {...newProps} />);

    return { component };
  }

  it('should exist', () => {
    const { component } = setup();
    expect(component.type()).toBe('div');
  });
});
