import chai from 'chai';
import _ from 'lodash';

import { serializePerson } from 'serializers/persons-serializer';
import { rawPerson, serializedPerson } from './mock-data';

chai.should();

describe('Test persons-serializer', () => {
  it('serializePerson should return data in proper JSON API format', () => {
    const result = serializePerson(rawPerson);
    return result.should.deep.equal(serializedPerson);
  });

  const testObjectProperties = [
    'citizenCode',
    'citizenDescription',
    'employeeStatusCode',
  ];
  _.forEach(testObjectProperties, (property) => {
    it(`serializePerson should remove ${property}`, () => {
      const result = serializePerson(rawPerson);
      return result.should.not.have.nested.property(`data.attributes.${property}`);
    });
  });
});
