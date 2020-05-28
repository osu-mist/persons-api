import chai from 'chai';
import _ from 'lodash';
import proxyquire from 'proxyquire';

import { rawAddress, fakeOsuId } from './mock-data';
import { testSingleResource, testMultipleResources } from './test-helpers';

chai.should();

describe('Test addresses-serializer', () => {
  // Using proxyquire to avoid issues with config in imported classes
  let serialProxy;
  beforeEach(() => {
    serialProxy = proxyquire('serializers/addresses-serializer', {});
  });

  it('test', () => {
    const result = serialProxy.serializeAddress(rawAddress, fakeOsuId);
    return testSingleResource(result, 'addresses', _.omit(rawAddress, ['addressId']));
  });

  it('test2', () => {
    const result = serialProxy.serializeAddresses([rawAddress, rawAddress], {}, fakeOsuId);
    return testMultipleResources(result);
  });
});
