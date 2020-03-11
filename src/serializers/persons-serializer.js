/* eslint-disable no-unused-vars */
import { Serializer as JsonApiSerializer } from 'jsonapi-serializer';
import _ from 'lodash';
import merge from 'merge-deep';

import { serializerOptions } from 'utils/jsonapi';
import { openapi } from 'utils/load-openapi';
import { apiBaseUrl, resourcePathLink, paramsLink } from 'utils/uri-builder';

const personResourceProp = openapi.components.schemas.PersonResult.properties.data.properties;
const personResourceType = personResourceProp.type.enum[0];
const personResourceAttributes = personResourceProp.attributes.allOf;
const personCombinedAttributes = merge(personResourceAttributes[0], personResourceAttributes[1]);
const personResourceKeys = _.keys(personCombinedAttributes.properties);
const personResourcePath = 'persons';
const personResourceUrl = resourcePathLink(apiBaseUrl, personResourcePath);

/**
 * Takes raw person data and serializes it into json api standards
 *
 * @param {object[]} rawPersons Raw person data from data source
 * @param {object} querys Query parameters from request
 * @returns {object} Serialized person resource data
 */
const serializePerson = (rawPerson) => {
  const topLevelSelfLink = resourcePathLink(personResourceUrl, rawPerson.osuId);
  const serializerArgs = {
    identifierField: 'osuId',
    resourceKeys: personResourceKeys,
    resourcePath: personResourcePath,
    topLevelSelfLink,
    enableDataLinks: true,
  };

  return new JsonApiSerializer(
    personResourceType,
    serializerOptions(serializerArgs, personResourcePath, topLevelSelfLink),
  ).serialize(rawPerson);
};

export {
  serializePerson,
};
