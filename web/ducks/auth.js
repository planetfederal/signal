import jwtDecode from 'jwt-decode';
import * as request from 'superagent-bluebird-promise';
import { push } from 'react-router-redux';
import { API_URL } from 'config';

export const LOGIN_USER_REQUEST = 'sc/auth/LOGIN_USER_REQUEST';
export const LOGIN_USER_FAILURE = 'sc/auth/LOGIN_USER_FAILURE';
export const LOGIN_USER_SUCCESS = 'sc/auth/LOGIN_USER_SUCCESS';
export const SIGNUP_USER_REQUEST = 'sc/auth/SIGNUP_USER_REQUEST';
export const SIGNUP_USER_FAILURE = 'sc/auth/SIGNUP_USER_FAILURE';
export const SIGNUP_USER_SUCCESS = 'sc/auth/SIGNUP_USER_SUCCESS';
export const LOGOUT_USER = 'sc/auth/LOGOUT_USER';
export const FETCH_PROTECTED_DATA_REQUEST = 'sc/auth/FETCH_PROTECTED_DATA_REQUEST';
export const RECEIVE_PROTECTED_DATA = 'sc/auth/RECEIVE_PROTECTED_DATA';
export const CHANGE_TEAM = 'sc/auth/CHANGE_TEAM';
export const LOAD_TEAMS = 'sc/auth/LOAD_TEAMS';
export const JOIN_TEAM = 'sc/auth/JOIN_TEAM';
export const LEAVE_TEAM = 'sc/auth/LEAVE_TEAM';

const initialState = {
  user: {},
  token: null,
  isAuthenticated: false,
  isAuthenticating: false,
  statusText: false,
  isSigningUp: false,
  signUpError: null,
  signUpSuccess: false,
  selectedTeamId: null,
  addTeamError: false,
};

export default function reducer(state = initialState, action = {}) {
  switch (action.type) {
    case LOGIN_USER_REQUEST:
      return {
        ...state,
        isAuthenticating: true,
        statusText: null,
      };
    case LOGIN_USER_SUCCESS:
      return {
        ...state,
        isAuthenticating: false,
        isAuthenticated: true,
        token: action.token,
        user: action.user,
        selectedTeamId: action.user.teams && action.user.teams.length ?
          action.user.teams[0].id : null,
        statusText: null,
      };
    case LOGIN_USER_FAILURE:
      return {
        ...state,
        isAuthenticating: false,
        isAuthenticated: false,
        token: null,
        user: {},
        statusText: action.statusText,
      };
    case SIGNUP_USER_REQUEST:
      return {
        ...state,
        isSigningUp: true,
        signUpSuccess: false,
      };
    case SIGNUP_USER_FAILURE:
      return {
        ...state,
        isSigningUp: false,
        signUpError: action.error,
        signUpSuccess: false,
      };
    case SIGNUP_USER_SUCCESS:
      return {
        ...state,
        isSigningUp: false,
        signUpError: null,
        signUpSuccess: true,
      };
    case LOGOUT_USER:
      return {
        ...state,
        isAuthenticated: false,
        token: null,
        user: {},
        statusText: null,
      };
    case CHANGE_TEAM:
      return {
        ...state,
        selectedTeamId: action.payload.teamId,
      };
    case JOIN_TEAM:
      return {
        ...state,
        user: {
          ...state.user,
          teams: state.user.teams.concat(action.payload.team),
        },
        selectedTeamId: state.selectedTeamId ? state.selectedTeamId : action.payload.team.id,
      };
    case LEAVE_TEAM: {
      const teams = state.user.teams.filter(t => t.id !== action.payload.team.id);
      return {
        ...state,
        user: {
          ...state.user,
          teams,
        },
        selectedTeamId: teams.length ? state.selectedTeamId : null,
      };
    }
    default: return state;
  }
}

export function loginUserSuccess(token) {
  return {
    type: LOGIN_USER_SUCCESS,
    token,
    user: jwtDecode(token).user,
  };
}

export function loginPersistedUser(token, user) {
  return {
    type: LOGIN_USER_SUCCESS,
    token,
    user,
  };
}

export function loginUserFailure(error) {
  return {
    type: LOGIN_USER_FAILURE,
    statusText: error,
  };
}

export function signUpUserFailure(error) {
  return {
    type: SIGNUP_USER_FAILURE,
    error,
  };
}

export function signUpUserSuccess() {
  return {
    type: SIGNUP_USER_SUCCESS,
  };
}

export function loginUserRequest() {
  return {
    type: LOGIN_USER_REQUEST,
  };
}

export function signUpUserRequest() {
  return {
    type: SIGNUP_USER_REQUEST,
  };
}

export function logout() {
  return {
    type: LOGOUT_USER,
  };
}

export function logoutAndRedirect() {
  return (dispatch) => {
    dispatch(logout());
    dispatch(push('/login'));
  };
}

export function loginUser(email, password, redirect = '/') {
  return (dispatch) => {
    dispatch(loginUserRequest());
    return request
      .post(`${API_URL}authenticate`)
      .send({ email, password })
      .then((response) => {
        try {
          if (response.body.result.token) {
            dispatch(loginUserSuccess(response.body.result.token));
            dispatch(push(redirect));
          } else {
            dispatch(loginUserFailure('Login unsuccessful.'));
          }
        } catch (e) {
          dispatch(loginUserFailure('Invalid token'));
        }
      }, (response) => {
        if (response.body.error) {
          dispatch(loginUserFailure(response.body.error));
        } else {
          dispatch(loginUserFailure('Login unsuccessful.'));
        }
      });
  };
}

export function signUpUser(name, email, password) {
  return (dispatch) => {
    dispatch(signUpUserRequest());
    return request
      .post(`${API_URL}users`)
      .send({ name, email, password })
      .then(() => dispatch(signUpUserSuccess()))
      .catch((response) => {
        if (response.body.error.errors) {
          dispatch(signUpUserFailure(response.body.error.errors[0].message));
        } else if (response.body.error.message) {
          dispatch(signUpUserFailure(response.body.error.message));
        } else {
          dispatch(signUpUserFailure('Sign Up unsuccessful.'));
        }
      });
  };
}

export function joinTeam(team) {
  return {
    type: JOIN_TEAM,
    payload: { team },
  };
}

export function leaveTeam(team) {
  return {
    type: LEAVE_TEAM,
    payload: { team },
  };
}

export function changeTeam(teamId) {
  return {
    type: CHANGE_TEAM,
    payload: {
      teamId: parseInt(teamId, 10),
    },
  };
}
