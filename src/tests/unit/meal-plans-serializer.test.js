import chai from 'chai';
import proxyquire from 'proxyquire';

import {
  rawMealPlan,
  serializedMealPlan,
  serializedMealPlans,
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
    return result.should.deep.equal(serializedMealPlan);
  });

  it('serializeMealPlans should return multiple records in proper JSON API format', () => {
    const result = serialProxy.serializeMealPlans([rawMealPlan, rawMealPlan], fakeOsuId);
    return result.should.deep.equal(serializedMealPlans);
  });
});
