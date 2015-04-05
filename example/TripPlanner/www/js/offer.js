
var OFFER_ATTRIBUTES = [
    "uuid",
    "created",
    "partner_uuid",
    "long_desc",
    "short_desc",
    "store_name",
    "match_name",
    "store_address",
    "store_phone",
    "terms",
    "start_date",
    "end_date",
    "discount",
    "image_url"
];

function createOffer(createOfferObj, successCB, errorCB) {
    createOfferObj.type = "offer";
    createOfferObj.partner_uuid = PARTNER_UUID;
    createOfferObj.match_name = createOfferObj.store_name.replace(/\W/g, '')
    postEntity(createOfferObj, successCB, errorCB);
}

function readOffer(uuid, successCB, errorCB) {
    getEntity("offer", uuid, OFFER_ATTRIBUTES, successCB, errorCB);
}

function readMultipleOffers(attribute, value, successCB, errorCB) {
    getEntities("offer", attribute, value, OFFER_ATTRIBUTES, successCB, errorCB);
}

function updateOffer(uuid, updateOfferObj, successCB, errorCB) {
    updateOfferObj.uuid = uuid;
    updateOfferObj.type = "offer";
    modifyEntity(updateOfferObj, successCB, errorCB);
}

function deleteOffer(uuid, successCB, errorCB) {
    deleteEntity("offer", uuid, successCB, errorCB);
}

function deleteAllOffersBelongingToPartner(partnerUUID, successCB, errorCB) {
    deleteEntities("offer", "partner_uuid", partnerUUID, successCB, errorCB);
}

