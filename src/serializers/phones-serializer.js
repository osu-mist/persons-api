import { Serializer as JsonApiSerializer } from 'jsonapi-serializer';
import _ from 'lodash';

import { formatSubObjects } from 'utils/format-sub-objects';
import { serializerOptions } from 'utils/jsonapi';
import { openapi } from 'utils/load-openapi';
import { apiBaseUrl, resourcePathLink, paramsLink } from 'utils/uri-builder';

const phoneResourceProp = openapi.components.schemas.PhoneResult.properties.data.properties;
const phoneResourceType = phoneResourceProp.type.enum[0];
const phoneResourceAttributes = phoneResourceProp.attributes;
const phoneResourceKeys = _.keys(phoneResourceAttributes.properties);

/**
 * Creates fullPhoneNumber and prepares sub-objects for serializer
 *
 * @param {object[]} rawPhones raw data from data source
 */
const prepareRawPhones = (rawPhones) => {
  _.forEach(rawPhones, (phone) => {
    // set fullPhoneNumber only if phoneNumber is not null
    phone.fullPhoneNumber = phone.phoneNumber
      ? `${phone.areaCode || ''}${phone.phoneNumber}`
      : null;
    phone.primaryInd = phone.primaryInd === 'Y';
  });

  formatSubObjects(rawPhones);
};

/**
 * Creates serializer arguments
 *
 * @param {string} osuId OSU ID of person
 * @param {object} query Query parameters passed in with the request
 * @returns {object} Serializer arguments
 */
const getSerializerArgs = (osuId, query) => {
  const phoneResourcePath = `persons/${osuId}/${phoneResourceType}`;
  const phoneResourceUrl = resourcePathLink(apiBaseUrl, phoneResourcePath);
  const topLevelSelfLink = paramsLink(phoneResourceUrl, query);
  return {
    identifierField: 'phoneId',
    resourceKeys: phoneResourceKeys,
    resourcePath: phoneResourcePath,
    topLevelSelfLink,
    enableDataLinks: true,
  };
};

/**
 * Serializes raw phone data from data source
 *
 * @param {object[]} rawPhones raw data from data source
 * @param {string} osuId OSU ID of person
 * @param {object} query query parameters passed in with the request
 * @returns {Promise<object>} Serialized phone resource
 */
const serializePhones = (rawPhones, osuId, query) => {
  const serializerArgs = getSerializerArgs(osuId, query);

  prepareRawPhones(rawPhones);

  return new JsonApiSerializer(
    phoneResourceType,
    serializerOptions(serializerArgs, phoneResourceType, serializerArgs.topLevelSelfLink),
  ).serialize(rawPhones);
};

/**
 * Serialize single raw phone data from data source
 *
 * @param {object[]} rawPhone raw data from data source
 * @param {string} osuId OSU ID of person
 * @returns {Promise<object>} Serialized phone resource
 */
const serializePhone = (rawPhone, osuId) => {
  const serializerArgs = getSerializerArgs(osuId, {});
  serializerArgs.topLevelSelfLink = `${serializerArgs.topLevelSelfLink}/${rawPhone.phoneId}`;

  prepareRawPhones([rawPhone]);

  return new JsonApiSerializer(
    phoneResourceType,
    serializerOptions(serializerArgs, phoneResourceType, serializerArgs.topLevelSelfLink),
  ).serialize(rawPhone);
};

export { serializePhone, serializePhones };
