import { errorHandler, errorBuilder } from 'errors/errors';
// import { getMealPlansByOsuId } from 'db/oracledb/meal-plans-dao';
// import { serializeMealPlans } from 'serializers/meal-plans-serializer';
import { personExists } from 'db/oracledb/persons-dao';

/**
 * Get meal plan by meal plan ID
 *
 * @type {RequestHandler}
 */
const get = async (req, res) => {
  try {
    const { query, params: { osuId, mealPlanId } } = req;
    console.log(mealPlanId);
    console.log(query);
    console.log(mealPlanId);

    if (!await personExists(osuId)) {
      return errorBuilder(res, 404, 'A person with the specified OSU ID was not found.');
    }

    // const result = await getMealPlansByOsuId(osuId, query);
    // const serializedMealPlans = serializeMealPlans(result, osuId, query);
    const serializedMealPlan = undefined;

    return res.send(serializedMealPlan);
  } catch (err) {
    return errorHandler(res, err);
  }
};

export { get };
