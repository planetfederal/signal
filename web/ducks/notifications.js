import * as request from 'superagent-bluebird-promise';
import { API_URL } from 'config';

export const LOAD_NOTIFICATION = 'sc/processors/LOAD_NOTIFICATION';
export const LOAD_NOTIFICATIONS = 'sc/processors/LOAD_NOTIFICATIONS';

const initialState = {
  notifications: [],
};

export default function reducer(state = initialState, action = {}) {
  switch (action.type) {
    case LOAD_NOTIFICATION:
      return {
        ...state,
        notifications: state.notifications.concat(action.payload.notification),
      };
    case LOAD_NOTIFICATIONS:
      return {
        ...state,
        notifications: action.payload.notifications,
      };
    default:
      return state;
  }
}

export function receiveNotifications(notifications) {
  return {
    type: LOAD_NOTIFICATIONS,
    payload: { notifications },
  };
}

export function receiveNotification(notification) {
  return {
    type: LOAD_NOTIFICATION,
    payload: { notification },
  };
}

export function loadNotifications() {
  return dispatch =>
    request
      .get(`${API_URL}notifications`)
      .then(res => res.body.result)
      .then(data => dispatch(receiveNotifications(data)));
}

export function loadNotification(notificationId) {
  return (dispatch, getState) => {
    const { sc } = getState();
    const token = sc.auth.token;
    return request
      .get(`${API_URL}notifications/${notificationId}`)
      .set('Authorization', `Token ${token}`)
      .then(res => res.body.result)
      .then(data => dispatch(receiveNotification(data)));
  };
}
