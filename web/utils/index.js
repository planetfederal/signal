import { UserAuthWrapper } from 'redux-auth-wrapper';
import { push } from 'react-router-redux';
import find from 'lodash/find';

export function checkHttpStatus(response) {
  if (response.status >= 200 && response.status < 300) {
    return response;
  }

  const error = new Error(response.statusText);
  error.response = response;
  throw error;
}

export const requireAuthentication = UserAuthWrapper({
  authSelector: state => state.sc.auth,
  predicate: auth => auth.isAuthenticated,
  redirectAction: push,
  wrapperDisplayName: 'UserIsJWTAuthenticated',
});

export const isUrl = (s) => {
  const regexp = /(ftp|http|https):\/\/(\w+:{0,1}\w*@)?(\S+)(:[0-9]+)?(\/|\/([\w#!:.?+=&%@!\-\/]))?/;
  return regexp.test(s);
};

export const isEmail = (email) => {
  const re = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
  return re.test(email);
};

export const toKey = s =>
  s.toLowerCase().replace(/ /g, '_');

export const initForm = teams =>
  (form) => {
    const team = find(teams, { id: form.team_id });
    const newForm = {
      ...form,
      team_name: team ? team.name : null,
      value: {},
      deletedFields: [],
    };
    form.fields.forEach((field) => {
      if ({}.hasOwnProperty.call(field, 'initialValue')) {
        newForm.value[field.field_key] = field.initialValue;
      }
    });
    return newForm;
  };

export const initStore = teams =>
  (store) => {
    const team = find(teams, { id: store.team_id });
    return {
      ...store,
      team_name: team ? team.name : null,
    };
  };

export const loadState = () => {
  try {
    const serializedState = localStorage.getItem('user_state');
    if (serializedState === null) {
      return undefined;
    }
    return JSON.parse(serializedState);
  } catch (err) {
    return undefined;
  }
};

export const saveState = (state) => {
  try {
    const serializedState = JSON.stringify(state);
    localStorage.setItem('user_state', serializedState);
  } catch (err) {
    // Ignore write errors.
  }
};

export const emptyStore = {
  id: false,
  name: '',
  version: '1',
  uri: '',
  store_type: '',
  options: {},
  style: [{
    id: 'default',
    paint: {
      'fill-color': '#ff0000',
      'fill-opacity': 0.5,
      'line-color': '#ff0000',
      'line-opacity': 1,
      'icon-color': '#ff0000',
    },
  }],
};
