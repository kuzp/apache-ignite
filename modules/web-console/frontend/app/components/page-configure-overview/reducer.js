export const RECEIVE_CONFIGURE_OVERVIEW = Symbol('RECEIVE_CONFIGURE_OVERVIEW');

const defaults = {
    isLoading: false,
    clustersTable: []
};

export const reducer = (state = defaults, action) => {
    switch (action.type) {
        case RECEIVE_CONFIGURE_OVERVIEW:
            return Object.assign({}, state, {clustersTable: action.clustersTable});
        default:
            return state;
    }
};
