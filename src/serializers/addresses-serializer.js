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
 * Takes raw addresses data and serializes it into json api standards
 *
 * @param rawAddresses
 * @param query
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

  return new JsonApiSerializer(
    addressResourceType,
    serializerOptions(serializerArgs, addressResourcePath, topLevelSelfLink),
  ).serialize(rawAddresses);
};

export { serializeAddresses };
