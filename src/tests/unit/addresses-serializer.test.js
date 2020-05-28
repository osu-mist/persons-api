import chai from 'chai';
import _ from 'lodash';

import { serializeAddress, serializeAddresses } from 'serializers/addresses-serializer';
import { rawAddress, fakeOsuId } from './mock-data';
import { testSingleResource, testMultipleResources } from './test-helpers';

chai.should();

describe('Test addresses-serializer', () => {
  it('serializeAddress should return data in proper JSON API format', () => {
    const result = serializeAddress(rawAddress, fakeOsuId);
    return testSingleResource(result, 'addresses', _.omit(rawAddress, ['addressId']));
  });

  it('serializeAddresses should serialize data in an array format', () => {
    const result = serializeAddresses([rawAddress, rawAddress], {}, fakeOsuId);
    return testMultipleResources(result);
  });
});
