import { Serializer as JsonApiSerializer } from 'jsonapi-serializer';
import _ from 'lodash';

import { formatSubObjects } from 'utils/format-sub-objects';
import { serializerOptions } from 'utils/jsonapi';
import { openapi } from 'utils/load-openapi';
import { apiBaseUrl, resourcePathLink, paramsLink } from 'utils/uri-builder';

const addressResourceProp = openapi.components.schemas.AddressResult.properties.data.properties;
const addressResourceType = addressResourceProp.type.enum[0];
const addressResourceAttributes = addressResourceProp.attributes;
const addressResourceKeys = _.keys(addressResourceAttributes.properties);

/**
 * Some fields need to be massaged before the can be passed to the serializer
 *
 * @param {object} rawAddresses raw address data from data source
 */
const prepareRawData = (rawAddresses) => {
  formatSubObjects(rawAddresses);
};

/**
 * Get serializer arguments for JsonApiSerializer
 *
 * @param {string} osuId
 * @param {object} query
 * @param {string} addressId ID of address record
 * @returns {object} serializer arguments
 */
const getSerializerArgs = (osuId, query) => {
  const addressResourcePath = `persons/${osuId}/${addressResourceType}`;
  const addressResourceUrl = resourcePathLink(apiBaseUrl, addressResourcePath);
  const topLevelSelfLink = paramsLink(addressResourceUrl, query);
  return {
    identifierField: 'addressId',
    resourceKeys: addressResourceKeys,
    resourcePath: addressResourcePath,
    topLevelSelfLink,
    enableDataLinks: true,
  };
};

/**
 * Takes raw addresses data and serializes it into json api standards
 *
 * @param {object} rawAddresses raw address data from data source
 * @param {object} query query parameters from request
 * @param {string} osuId OSU ID for a person
 * @returns {object}
 */
const serializeAddresses = (rawAddresses, query, osuId) => {
  const serializerArgs = getSerializerArgs(osuId, query);

  prepareRawData(rawAddresses);

  return new JsonApiSerializer(
    addressResourceType,
    serializerOptions(serializerArgs, addressResourceType, serializerArgs.topLevelSelfLink),
  ).serialize(rawAddresses);
};

/**
 * Takes a raw address record and serializes it into json api standards
 *
 * @param {object} rawAddress
 * @param {string} osuId
 * @returns {object} serialized address data
 */
const serializeAddress = (rawAddress, osuId) => {
  const serializerArgs = getSerializerArgs(osuId, null);
  serializerArgs.topLevelSelfLink = `${serializerArgs.topLevelSelfLink}/${rawAddress.addressId}`;

  prepareRawData([rawAddress]);

  return new JsonApiSerializer(
    addressResourceType,
    serializerOptions(serializerArgs, addressResourceType, serializerArgs.topLevelSelfLink),
  ).serialize(rawAddress);
};

export { serializeAddress, serializeAddresses };
