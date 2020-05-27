import chai from 'chai';
import _ from 'lodash';
import proxyquire from 'proxyquire';

import { rawPerson, serializedPerson } from './mock-data';

chai.should();

describe('Test persons-serializer', () => {
  // Using proxyquire to avoid issues with config in imported classes
  let serialProxy;
  beforeEach(() => {
    serialProxy = proxyquire('serializers/persons-serializer', {});
  });

  it('serializePerson should return data in proper JSON API format', () => {
    const result = serialProxy.serializePerson(rawPerson);
    return result.should.deep.equal(serializedPerson);
  });

  const testObjectProperties = [
    'citizenCode',
    'citizenDescription',
    'employeeStatusCode',
  ];
  _.forEach(testObjectProperties, (property) => {
    it(`serializePerson should remove ${property}`, () => {
      const result = serialProxy.serializePerson(rawPerson);
      return result.should.not.have.nested.property(`data.attributes.${property}`);
    });
  });
});
