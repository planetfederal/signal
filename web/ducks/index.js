import { combineReducers } from 'redux';
import auth from './auth';
import processors from './processors';
import menu from './menu';
import notifications from './notifications';

// http://redux.js.org/docs/api/combineReducers.html
const appReducer = combineReducers({
  auth,
  processors,
  menu,
  notifications,
});

export default appReducer;
