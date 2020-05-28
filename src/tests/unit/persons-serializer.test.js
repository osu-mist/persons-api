import chai from 'chai';
import _ from 'lodash';
import proxyquire from 'proxyquire';

import { rawPerson } from './mock-data';
import { testSingleResource } from './test-helpers';

chai.should();

describe('Test persons-serializer', () => {
  // Using proxyquire to avoid issues with config in imported classes
  let serialProxy;
  beforeEach(() => {
    serialProxy = proxyquire('serializers/persons-serializer', {});
  });

  const removedProperties = [
    'citizenCode',
    'citizenDescription',
    'employeeStatusCode',
  ];

  it('serializePerson should return data in proper JSON API format', () => {
    const result = serialProxy.serializePerson(rawPerson);
    return testSingleResource(result, 'person', _.omit(rawPerson, ['osuId', ...removedProperties]));
  });

  _.forEach(removedProperties, (property) => {
    it(`serializePerson should remove ${property}`, () => {
      const result = serialProxy.serializePerson(rawPerson);
      return result.should.not.have.nested.property(`data.attributes.${property}`);
    });
  });
});
