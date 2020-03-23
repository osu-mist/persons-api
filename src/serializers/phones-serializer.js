import { Serializer as JsonApiSerializer } from 'jsonapi-serializer';
import _ from 'lodash';

import { serializerOptions } from 'utils/jsonapi';
import { openapi } from 'utils/load-openapi';
import { apiBaseUrl, resourcePathLink, paramsLink } from 'utils/uri-builder';

const phoneResourceProp = openapi.components.schemas.PhoneResult.properties.data.properties;
const phoneResourceType = phoneResourceProp.type.enum[0];
const phoneResourceAttributes = phoneResourceProp.attributes.allOf;
const phoneCombinedAttributes = _.merge(phoneResourceAttributes[0], phoneResourceAttributes[1]);
const phoneResourceKeys = _.keys(phoneCombinedAttributes.properties);

const getSerializerArgs = (osuId, query) => {
  const phoneResourcePath = `persons/${osuId}/${phoneResourceType}`;
  const addressResourceUrl = resourcePathLink(apiBaseUrl, phoneResourcePath);
  const topLevelSelfLink = paramsLink(addressResourceUrl, query);
  return {
    identifierField: 'phoneId',
    resourceKeys: phoneResourceKeys,
    resourcePath: phoneResourcePath,
    topLevelSelfLink,
    enableDataLinks: true,
  };
};

const serializePhones = (rawPhones, osuId, query) => {
  const serializerArgs = getSerializerArgs(osuId, query);

  return new JsonApiSerializer(
    phoneResourceType,
    serializerOptions(serializerArgs, phoneResourceType, serializerArgs.topLevelSelfLink),
  ).serialize(rawPhones);
};

export { serializePhones };
