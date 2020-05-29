import chai from 'chai';
import proxyquire from 'proxyquire';

import { testSingleResource, testMultipleResources } from './test-helpers';

import {
  rawMealPlan,
  fakeOsuId,
} from './mock-data';

chai.should();

describe('Test meal-plans-serializer', () => {
  // Using proxyquire to avoid issues with config in imported classes
  let serialProxy;
  beforeEach(() => {
    serialProxy = proxyquire('serializers/meal-plans-serializer', {});
  });

  it('serializeMealPlan should return singular data in proper JSON API format', () => {
    const result = serialProxy.serializeMealPlan(rawMealPlan, fakeOsuId);
    return testSingleResource(result, 'meal-plans');
  });

  it('serializeMealPlans should return multiple records in proper JSON API format', () => {
    const result = serialProxy.serializeMealPlans([rawMealPlan, rawMealPlan], fakeOsuId);
    return testMultipleResources(result);
  });
});
