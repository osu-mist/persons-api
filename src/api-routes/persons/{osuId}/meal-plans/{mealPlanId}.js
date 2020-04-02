import { errorHandler, errorBuilder } from 'errors/errors';
import { getMealPlanByMealPlanId } from 'db/oracledb/meal-plans-dao';
import { serializeMealPlan } from 'serializers/meal-plans-serializer';
import { personExists } from 'db/oracledb/persons-dao';

/**
 * Get meal plan by meal plan ID
 *
 * @type {RequestHandler}
 */
const get = async (req, res) => {
  try {
    const { query, params: { osuId, mealPlanId } } = req;

    if (!await personExists(osuId)) {
      return errorBuilder(res, 404, 'A person with the specified OSU ID was not found.');
    }

    const result = await getMealPlanByMealPlanId(osuId, mealPlanId);
    if (!result) {
      return errorBuilder(res, 404, 'A meal-plan with the specified meal-plan ID was not found.');
    }
    const serializedMealPlan = serializeMealPlan(result, osuId, query);

    return res.send(serializedMealPlan);
  } catch (err) {
    return errorHandler(res, err);
  }
};

export { get };
