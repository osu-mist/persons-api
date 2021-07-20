import { Serializer as JsonApiSerializer } from 'jsonapi-serializer';
import _ from 'lodash';

import { formatSubObjects } from 'utils/format-sub-objects';
import { serializerOptions } from 'utils/jsonapi';
import { openapi } from 'utils/load-openapi';
import { apiBaseUrl, resourcePathLink, paramsLink } from 'utils/uri-builder';

const medicalResourceProp = openapi.components.schemas.MedicalResource.properties;
const medicalResourceType = medicalResourceProp.type.enum[0];
const medicalResourceAttributes = medicalResourceProp.attributes;
const medicalResourceKeys = _.keys(medicalResourceAttributes.properties);

/**
 * Get serializer arguments for JsonApiSerializer
 *
 * @param {string} osuId
 * @param {object} query
 * @returns {object} serializer arguments
 */
const getSerializerArgs = (osuId, query) => {
  const medicalResourcePath = `persons/${osuId}/${medicalResourceType}`;
  const medicalResourceUrl = resourcePathLink(apiBaseUrl, medicalResourcePath);
  const topLevelSelfLink = paramsLink(medicalResourceUrl, query);
  return {
    identifierField: 'medicalId',
    resourceKeys: medicalResourceKeys,
    resourcePath: medicalResourcePath,
    topLevelSelfLink,
    enableDataLinks: true,
  };
};

/**
 * Uses JSONAPI serializer to serialize raw data from data source
 *
 * @param {object[]} rawMedical raw data from data source
 * @param {string} osuId OSU ID of a person
 * @param {object} query query parameters passed in with request
 * @returns {object} serialized data
 */
const serializeMedical = (rawMedical, osuId, query) => {
  const serializerArgs = getSerializerArgs(osuId, query);

  formatSubObjects(rawMedical);

  return new JsonApiSerializer(
    medicalResourceType,
    serializerOptions(serializerArgs, medicalResourceType, serializerArgs.topLevelSelfLink),
  ).serialize(rawMedical);
};

export { serializeMedical };
