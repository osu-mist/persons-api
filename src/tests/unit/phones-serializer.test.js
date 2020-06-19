import chai from 'chai';
import _ from 'lodash';
import proxyquire from 'proxyquire';

import { rawPhone, fakeOsuId } from './mock-data';
import { testSingleResource, testMultipleResources, createConfigStub } from './test-helpers';

chai.should();

describe('Test phones-serializer', () => {
  const configStub = createConfigStub();
  const { serializePhone, serializePhones } = proxyquire(
    'serializers/phones-serializer',
    {},
  );
  configStub.restore();

  it('serializePhone should return data in proper JSON API format', () => {
    const result = serializePhone(rawPhone, fakeOsuId);
    return testSingleResource(result, 'phones', _.omit(rawPhone, ['phoneId']));
  });

  it('serializePhones should serialize data in an array format', () => {
    const result = serializePhones([rawPhone, rawPhone], {}, fakeOsuId);
    return testMultipleResources(result);
  });
});
