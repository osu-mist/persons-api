import { Serializer as JsonApiSerializer } from 'jsonapi-serializer';
import _ from 'lodash';

import { serializerOptions } from 'utils/jsonapi';
import { openapi } from 'utils/load-openapi';
import { apiBaseUrl, resourcePathLink, paramsLink } from 'utils/uri-builder';
// import { contrib } from '../db/oracledb/contrib/contrib';

const addressResourceProp = openapi.components.schemas.AddressResult.properties.data.properties;
const addressResourceType = addressResourceProp.type.enum[0];
const addressResourceAttributes = addressResourceProp.attributes.allOf;
const addressCombinedAttributes = _.merge(
  addressResourceAttributes[0], addressResourceAttributes[1],
);
const addressResourceKeys = _.keys(addressCombinedAttributes.properties);

/**
 * Some fields need to be massaged before the can be passed to the serializer
 *
 * @param {object} rawAddresses raw address data from data source
 */
const prepareRawData = (rawAddresses) => {
  // handle sub objects programmatically
  _.forEach(rawAddresses, (address) => {
    _.forEach(address, (value, key) => {
      const splitKey = key.split('.');
      if (splitKey.length > 1) {
        if (!address[splitKey[0]]) {
          address[splitKey[0]] = {};
        }
        address[splitKey[0]][splitKey[1]] = value;
      }
    });
  });
};

/**
 * Get serializer arguments for JsonApiSerializer
 *
 * @param {string} osuId
 * @param {object} query
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

export { serializeAddresses };
