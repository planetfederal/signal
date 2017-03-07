import { combineReducers } from 'redux';
import dataStores from './dataStores';
import auth from './auth';
import triggers from './triggers';
import menu from './menu';
import notifications from './notifications';

// http://redux.js.org/docs/api/combineReducers.html
const appReducer = combineReducers({
  dataStores,
  auth,
  triggers,
  menu,
  notifications,
});

export default appReducer;
