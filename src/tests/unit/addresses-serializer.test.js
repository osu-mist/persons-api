import chai from 'chai';
import _ from 'lodash';
import proxyquire from 'proxyquire';

import { rawAddress, fakeOsuId } from './mock-data';
import { testSingleResource, testMultipleResources, createConfigStub } from './test-helpers';

chai.should();

describe('Test addresses-serializer', () => {
  const configStub = createConfigStub();
  const { serializeAddress, serializeAddresses } = proxyquire(
    'serializers/addresses-serializer',
    {},
  );
  configStub.restore();

  it('serializeAddress should return data in proper JSON API format', () => {
    const result = serializeAddress(rawAddress, fakeOsuId);
    return testSingleResource(result, 'addresses', _.omit(rawAddress, ['addressId']));
  });

  it('serializeAddresses should serialize data in an array format', () => {
    const result = serializeAddresses([rawAddress, rawAddress], {}, fakeOsuId);
    return testMultipleResources(result);
  });
});
