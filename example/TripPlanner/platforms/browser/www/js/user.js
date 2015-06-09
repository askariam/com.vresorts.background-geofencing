
var USER_ATTRIBUTES = [
    "uuid",
    "created",
    "username",
    "password",
];

var USER_DETAIL_ATTRIBUTES = [
    "uuid",
    "created",
    "username",
    "email"
];

function userLogin(loginObj, successCB, errorCB) {
    doUserLogin(loginObj, successCB, errorCB);
}

function createUser(createUserObj, successCB, errorCB) {
    postNewUser(createUserObj, successCB, errorCB);
}

