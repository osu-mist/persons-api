import { Serializer as JsonApiSerializer } from 'jsonapi-serializer';
import _ from 'lodash';

import { formatSubObjects } from 'utils/format-sub-objects';
import { serializerOptions } from 'utils/jsonapi';
import { openapi } from 'utils/load-openapi';
import { apiBaseUrl, resourcePathLink, paramsLink } from 'utils/uri-builder';

const emailResourceProp = openapi.components.schemas.EmailResource.properties;
const emailResourceType = emailResourceProp.type.enum[0];
const emailResourceAttributes = emailResourceProp.attributes.allOf;
const emailCombinedAttributes = _.merge(emailResourceAttributes[0], emailResourceAttributes[1]);
const emailResourceKeys = _.keys(emailCombinedAttributes.properties);

/**
 * Some fields need to be prepared for the serializer
 *
 * @param {object[]} rawEmails raw data from data source
 */
const prepareRawEmails = (rawEmails) => {
  _.forEach(rawEmails, (email) => {
    email.preferredInd = email.preferredInd === 'Y';
  });

  formatSubObjects(rawEmails);
};

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

/**
 * Uses JSONAPI serializer to serialize raw data from data source
 *
 * @param {object[]} rawEmails raw data from data source
 * @param {string} osuId OSU ID of a person
 * @param {object} query query parameters passed in with request
 * @returns {object} serialized data
 */
const serializeEmails = (rawEmails, osuId, query) => {
  const serializerArgs = getSerializerArgs(osuId, query);

  prepareRawEmails(rawEmails);

  return new JsonApiSerializer(
    emailResourceType,
    serializerOptions(serializerArgs, emailResourceType, serializerArgs.topLevelSelfLink),
  ).serialize(rawEmails);
};

export { serializeEmails };
