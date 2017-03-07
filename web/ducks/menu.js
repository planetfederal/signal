export const TOGGLE_MENU = 'sc/menu/TOGGLE_MENU';
export const CLOSE_MENU = 'sc/menu/CLOSE_MENU';
export const OPEN_MENU = 'sc/menu/OPEN_MENU';

const initialState = {
  open: false,
};

export default function reducer(state = initialState, action = {}) {
  switch (action.type) {
    case TOGGLE_MENU:
      return {
        ...state,
        open: !state.open,
      };
    case CLOSE_MENU:
      return {
        ...state,
        open: false,
      };
    case OPEN_MENU:
      return {
        ...state,
        open: true,
      };
    default: return state;
  }
}

export function toggleMenu() {
  return {
    type: TOGGLE_MENU,
  };
}

export function closeMenu() {
  return {
    type: CLOSE_MENU,
  };
}

export function openMenu() {
  return {
    type: OPEN_MENU,
  };
}

