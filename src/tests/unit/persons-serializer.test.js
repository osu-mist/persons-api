import chai from 'chai';
import _ from 'lodash';

import { serializePerson } from 'serializers/persons-serializer';
import { rawPerson } from './mock-data';
import { testSingleResource } from './test-helpers';

chai.should();

describe('Test persons-serializer', () => {
  const removedProperties = [
    'citizenCode',
    'citizenDescription',
    'employeeStatusCode',
  ];

  it('serializePerson should return data in proper JSON API format', () => {
    const result = serializePerson(rawPerson);
    return testSingleResource(result, 'person', _.omit(rawPerson, ['osuId', ...removedProperties]));
  });

  _.forEach(removedProperties, (property) => {
    it(`serializePerson should remove ${property}`, () => {
      const result = serializePerson(rawPerson);
      return result.should.not.have.nested.property(`data.attributes.${property}`);
    });
  });
});
