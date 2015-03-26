
var CLIENT;
var USER_UUID = "0";
var PARTNER_UUID = "0";
var ORG_NAME = "vresorts";
var APP_NAME = "futuretravel";

function doUserLogin(loginObj, successCB, errorCB) {
    var clientCreds = {
        orgName:ORG_NAME,
        appName:APP_NAME,
        logging: true,
        buildCurl: true
    };

    CLIENT = new Apigee.Client(clientCreds);

    CLIENT.login(loginObj.username, loginObj.password, function (error,response) {
        if (error) {
            errorCB();
        }
        else {
            CLIENT.getLoggedInUser( function(error, data, user) {
                if(!error && (user.get("account_type") == "user") ) {
                    USER_UUID = user.get("uuid");
                    // If local storage is supported by this browser, this writes the
                    // USER_UUID to it, to enable adding places to trip plans. If local
                    // storage is not supported, this step will be skipped and adding
                    // places to trip plans won't work, but the rest of the site will work.
                    if(typeof(Storage)!=="undefined") {
                        localStorage.userUUID = USER_UUID;
                    }
                    successCB();
                }
                else {
                    errorCB();
                }
            });
        }
    });
}

// Posts a new user account. Both callbacks have no arguments.
function postNewUser(createUserObj, successCB, errorCB) {
    var createUserData = {};
    createUserData.type = "users";
    createUserData.account_type = "user";
    createUserData.username = createUserObj.username;
    createUserData.password = createUserObj.password;

    var createUserDetailData = {};
    createUserDetailData.type = "user_detail";
    createUserDetailData.username = createUserObj.username;
    createUserDetailData.email = createUserObj.email;

    var clientCreds = {
        orgName:ORG_NAME,
        appName:APP_NAME,
        logging: true,
        buildCurl: true
    };

    var tempClient = new Apigee.Client(clientCreds);

    tempClient.createEntity(createUserData, function(error, response) {
        if(error) {
            errorCB();
        }
        else {
            createUserDetailData.user_uuid = response._data.uuid;
            tempClient.createEntity(createUserDetailData, function(error, response) {
                if(error) {
                    errorCB();
                }
                else {
                    successCB();
                }
            });
        }
    });

    tempClient.logout();
}

function doPartnerLogin(loginObj, successCB, errorCB) {
    var clientCreds = {
        orgName:ORG_NAME,
        appName:APP_NAME,
        logging: true,
        buildCurl: true
    };

    CLIENT = new Apigee.Client(clientCreds);

    CLIENT.login(loginObj.username, loginObj.password, function (error,response) {
        if (error) {
            errorCB();
        }
        else {
            CLIENT.getLoggedInUser( function(error, data, user) {
                if(!error && (user.get("account_type") == "partner") ) {
                    PARTNER_UUID = user.get("uuid");
                    successCB();
                }
                else {
                    errorCB();
                }
            });
        }
    });
}

// Posts a new partner account. Both callbacks have no arguments.
function postNewPartner(createPartnerObj, successCB, errorCB) {
    var createPartnerData = {};
    createPartnerData.type = "users";
    createPartnerData.account_type = "partner";
    createPartnerData.username = createPartnerObj.username;
    createPartnerData.password = createPartnerObj.password;

    var createPartnerDetailData = {};
    createPartnerDetailData.type = "partner_detail";
    createPartnerDetailData.username = createPartnerObj.username;
    createPartnerDetailData.email = createPartnerObj.email;
    createPartnerDetailData.company_name = createPartnerObj.company_name;
    createPartnerDetailData.phone = createPartnerObj.phone;
    createPartnerDetailData.address = createPartnerObj.address;

    var clientCreds = {
        orgName:ORG_NAME,
        appName:APP_NAME,
        logging: true,
        buildCurl: true
    };

    var tempClient = new Apigee.Client(clientCreds);

    tempClient.createEntity(createPartnerData, function(error, response) {
        if(error) {
            errorCB();
        }
        else {
            createPartnerDetailData.partner_uuid = response._data.uuid;
            tempClient.createEntity(createPartnerDetailData, function(error, response) {
                if(error) {
                    errorCB();
                }
                else {
                    successCB();
                }
            });
        }
    });

    tempClient.logout();
}

function doLogout() {
    CLIENT.logout();
}

function doGuestLogin() {
    var clientCreds = {
        orgName:ORG_NAME,
        appName:APP_NAME,
        logging: true,
        buildCurl: true
    };

    CLIENT = new Apigee.Client(clientCreds);
}

function checkUsername(username, callback) {
    if (typeof CLIENT == "undefined") {
       doGuestLogin();
    }

    getEntities("user", "username", username, ["username"],
        function (userList) {
            if(userList.length > 0) {
                callback(true);
            }
        },
        function () {
            callback(false);
        }
    );
}

