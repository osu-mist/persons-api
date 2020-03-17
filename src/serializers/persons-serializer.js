import { Serializer as JsonApiSerializer } from 'jsonapi-serializer';
import _ from 'lodash';

import { serializerOptions } from 'utils/jsonapi';
import { openapi } from 'utils/load-openapi';
import { apiBaseUrl, resourcePathLink } from 'utils/uri-builder';
import { contrib } from '../db/oracledb/contrib/contrib';

const personResourceProp = openapi.components.schemas.PersonResult.properties.data.properties;
const personResourceType = personResourceProp.type.enum[0];
const personResourceAttributes = personResourceProp.attributes.allOf;
const personCombinedAttributes = _.merge(personResourceAttributes[0], personResourceAttributes[1]);
const personResourceKeys = _.keys(personCombinedAttributes.properties);
const personResourcePath = 'persons';
const personResourceUrl = resourcePathLink(apiBaseUrl, personResourcePath);

/**
 * Some fields need to be massaged before they can be passed to the serializer
 *
 * @param {Object} rawPerson Raw person data from data source
 */
const prepareRawData = (rawPerson) => {
  rawPerson.confidentialInd = rawPerson.confidentialInd === 'Y';
  rawPerson.currentStudentInd = rawPerson.currentStudentInd === 'Y';

  rawPerson.employeeStatus = {
    code: rawPerson.employeeStatusCode,
    description: contrib.getEmployeeStatusDescrByCode(rawPerson.employeeStatusCode),
  };

  rawPerson.citizen = {
    code: rawPerson.citizenCode,
    description: rawPerson.citizenDescription,
  };
};

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

  prepareRawData(rawPerson);

  return new JsonApiSerializer(
    personResourceType,
    serializerOptions(serializerArgs, personResourcePath, topLevelSelfLink),
  ).serialize(rawPerson);
};

export { serializePerson };
