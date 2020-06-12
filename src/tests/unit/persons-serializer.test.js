import chai from 'chai';
import _ from 'lodash';
import proxyquire from 'proxyquire';

import { rawPerson } from './mock-data';
import { testSingleResource, createConfigStub } from './test-helpers';

chai.should();

describe('Test persons-serializer', () => {
  const configStub = createConfigStub();
  const { serializePerson } = proxyquire(
    'serializers/persons-serializer',
    {},
  );
  configStub.restore();

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
