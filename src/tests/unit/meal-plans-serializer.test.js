import chai from 'chai';

import { serializeMealPlan, serializeMealPlans } from 'serializers/meal-plans-serializer';
import { rawMealPlan, fakeOsuId } from './mock-data';
import { testSingleResource, testMultipleResources } from './test-helpers';

chai.should();

describe('Test meal-plans-serializer', () => {
  it('serializeMealPlan should return singular data in proper JSON API format', () => {
    const result = serializeMealPlan(rawMealPlan, fakeOsuId);
    return testSingleResource(result, 'meal-plans');
  });

  it('serializeMealPlans should return multiple records in proper JSON API format', () => {
    const result = serializeMealPlans([rawMealPlan, rawMealPlan], fakeOsuId);
    return testMultipleResources(result);
  });
});
