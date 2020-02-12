/* eslint-disable no-unused-vars */
import { Serializer as JsonApiSerializer } from 'jsonapi-serializer';
import _ from 'lodash';

import { serializerOptions } from 'utils/jsonapi';
import { openapi } from 'utils/load-openapi';
import { paginate } from 'utils/paginator';
import { apiBaseUrl, resourcePathLink, paramsLink } from 'utils/uri-builder';

const personResourceProp = openapi.definitions.PersonResultObject.properties.data.properties;
const personResourceType = personResourceProp.type.example;
const personResourceKeys = _.keys(personResourceProp.attributes.properties);
const personResourcePath = 'person';
const personResourceUrl = resourcePathLink(apiBaseUrl, personResourcePath);

const serializePerson = (rawPerson) => {
  const serializedPerson = {};
  const topLevelSelfLink = resourcePathLink(personResourceUrl, rawPerson.osuId);
  const serializerArgs = {
    identifierField: 'OSU_ID',
    resourceKeys: personResourceKeys,
    resourcePath: personResourcePath,
    topLevelSelfLink,
    enableDataLinks: true,
  };

  serializedPerson.links = { topLevelSelfLink };
  serializedPerson.data = { attributes: rawPerson };

  return new JsonApiSerializer(
    personResourceType,
    serializerOptions(serializerArgs, personResourcePath, topLevelSelfLink),
  ).serialize(rawPerson);
};

export {
  serializePerson,
};
