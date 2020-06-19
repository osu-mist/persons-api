import chai from 'chai';
import proxyquire from 'proxyquire';

import { rawMealPlan, fakeOsuId } from './mock-data';
import { testSingleResource, testMultipleResources, createConfigStub } from './test-helpers';

chai.should();

describe('Test meal-plans-serializer', () => {
  const configStub = createConfigStub();
  const { serializeMealPlan, serializeMealPlans } = proxyquire(
    'serializers/meal-plans-serializer',
    {},
  );
  configStub.restore();

  it('serializeMealPlan should return singular data in proper JSON API format', () => {
    const result = serializeMealPlan(rawMealPlan, fakeOsuId);
    return testSingleResource(result, 'meal-plans');
  });

  it('serializeMealPlans should return multiple records in proper JSON API format', () => {
    const result = serializeMealPlans([rawMealPlan, rawMealPlan], fakeOsuId);
    return testMultipleResources(result);
  });
});
