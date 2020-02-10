/* eslint-disable no-unused-vars */
import { Serializer as JsonApiSerializer } from 'jsonapi-serializer';
import _ from 'lodash';

import { serializerOptions } from 'utils/jsonapi';
import { openapi } from 'utils/load-openapi';
import { paginate } from 'utils/paginator';
import { apiBaseUrl, resourcePathLink, paramsLink } from 'utils/uri-builder';

const personResourceProp = openapi.definitions.PersonResultObject.properties.data.properties;
const personResourceType = personResourceProp.type.example;
const personResourcePath = 'person';
const personResourceUrl = resourcePathLink(apiBaseUrl, personResourcePath);

const serializePerson = (rawPerson) => {
  const topLevelSelfLink = resourcePathLink(personResourceUrl, rawPerson.Id);
};

export {
  serializePerson,
};
