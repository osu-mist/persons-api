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
 * Employee status code descriptions are not stored in a db so we must manage them
 *
 * @param {string} statusCode employee status code returned from data source
 * @returns {string} description for the passed in status code
 */
const getEmployeeStatusDescrByCode = (statusCode) => (
  {
    A: 'Active',
    B: 'Leave without pay but with benefits',
    L: 'Leave without pay and benefits',
    F: 'Leave with full pay and benefits',
    P: 'Leave with partial pay and benefits',
    T: 'Terminated',
  }[statusCode]
);

/**
 * Takes raw person data and serializes it into json api standards
 *
 * @param {object} rawPerson Raw person data from data source
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

  rawPerson.employeeStatus = {
    code: rawPerson.employeeStatusCode,
    description: getEmployeeStatusDescrByCode(rawPerson.employeeStatusCode),
  };

  rawPerson.citizen = {
    code: rawPerson.citizenCode,
    description: rawPerson.citizenDescription,
  };

  return new JsonApiSerializer(
    personResourceType,
    serializerOptions(serializerArgs, personResourcePath, topLevelSelfLink),
  ).serialize(rawPerson);
};

export {
  serializePerson,
};
