import { Serializer as JsonApiSerializer } from 'jsonapi-serializer';
import _ from 'lodash';

import { serializerOptions } from 'utils/jsonapi';
import { openapi } from 'utils/load-openapi';
import { apiBaseUrl, resourcePathLink, paramsLink } from 'utils/uri-builder';

const emailResourceProp = openapi.components.schemas.EmailResource.properties;
const emailResourceType = emailResourceProp.type.enum[0];
const emailResourceAttributes = emailResourceProp.attributes;
const emailResourceKeys = _.keys(emailResourceAttributes.properties);

/**
 * Get serializer arguments for JsonApiSerializer
 *
 * @param {string} osuId
 * @param {object} query
 * @returns {object} serializer arguments
 */
const getSerializerArgs = (osuId, query) => {
  const emailResourcePath = `persons/${osuId}/${emailResourceType}`;
  const emailResourceUrl = resourcePathLink(apiBaseUrl, emailResourcePath);
  const topLevelSelfLink = paramsLink(emailResourceUrl, query);
  return {
    identifierField: 'emailId',
    resourceKeys: emailResourceKeys,
    resourcePath: emailResourcePath,
    topLevelSelfLink,
    enableDataLinks: true,
  };
};

const serializeEmails = (rawEmails, osuId, query) => {
  const serializerArgs = getSerializerArgs(osuId, query);

  return new JsonApiSerializer(
    emailResourceType,
    serializerOptions(serializerArgs, emailResourceType, serializerArgs.topLevelSelfLink),
  ).serialize(rawEmails);
};

export { serializeEmails };
