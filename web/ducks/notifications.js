import * as request from 'superagent-bluebird-promise';
import { API_URL } from 'config';

export const LOAD_NOTIFICATION = 'sc/processors/LOAD_NOTIFICATION';

const initialState = {
  notifications: {},
};

export default function reducer(state = initialState, action = {}) {
  switch (action.type) {
    case LOAD_NOTIFICATION:
      return {
        ...state,
        notifications: {
          ...state.notifications,
          [action.payload.notification.id]: action.payload.notification,
        },
      };
    default: return state;
  }
}

export function receiveNotification(notification) {
  return {
    type: LOAD_NOTIFICATION,
    payload: { notification },
  };
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
