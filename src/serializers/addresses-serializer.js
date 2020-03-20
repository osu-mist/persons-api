/* eslint-disable no-unused-vars */
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
const addressResourcePath = 'addresses';
const addressResourceUrl = resourcePathLink(apiBaseUrl, addressResourcePath);

/**
 * Some fields need to be massaged before the can be passed to the serializer
 *
 * @param {*} rawAddresses raw address data from data source
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
 * Takes raw addresses data and serializes it into json api standards
 *
 * @param rawAddresses raw address data from data source
 * @param query query parameters from request
 * @returns {object}
 */
const serializeAddresses = (rawAddresses, query) => {
  const topLevelSelfLink = paramsLink(addressResourceUrl, query);
  const serializerArgs = {
    identifierField: 'addressId',
    resourceKeys: addressResourceKeys,
    resourcePath: addressResourcePath,
    topLevelSelfLink,
    enableDataLinks: true,
  };

  prepareRawData(rawAddresses);

  return new JsonApiSerializer(
    addressResourceType,
    serializerOptions(serializerArgs, addressResourcePath, topLevelSelfLink),
  ).serialize(rawAddresses);
};

export { serializeAddresses };
