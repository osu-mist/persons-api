/* eslint-disable no-unused-vars */
import { Serializer as JsonApiSerializer } from 'jsonapi-serializer';
import _ from 'lodash';

import { serializerOptions } from 'utils/jsonapi';
import { openapi } from 'utils/load-openapi';
import { apiBaseUrl, resourcePathLink, paramsLink } from 'utils/uri-builder';

const personResourceProp = openapi.definitions.PersonResultObject.properties.data.properties;
const personResourceType = personResourceProp.type.example;
const personResourceKeys = _.keys(personResourceProp.attributes.properties);
const personResourcePath = 'person';
const personResourceUrl = resourcePathLink(apiBaseUrl, personResourcePath);

const serializePersons = (rawPersons, querys) => {
  const topLevelSelfLink = paramsLink(personResourceUrl, {
    osuId: querys.osuId.length > 0 ? querys.osuId.join(',') : null,
    firstName: querys.firstName,
    lastName: querys.lastName,
    osuUid: querys.osuUid,
    onid: querys.onid,
  });
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
  ).serialize(rawPersons);
};

export {
  serializePersons,
};
