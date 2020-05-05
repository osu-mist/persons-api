import { getJobByJobId } from 'db/oracledb/jobs-dao';
import { personExists } from 'db/oracledb/persons-dao';
import { errorHandler, errorBuilder } from 'errors/errors';

/**
 * Get job by job ID
 *
 * @type {RequestHandler}
 */
const get = async (req, res) => {
  try {
    const { params: { osuId, jobId } } = req;

    const internalId = await personExists(osuId);
    if (!internalId) {
      return errorBuilder(res, 404, 'A person with the specified OSU ID was not found.');
    }

    const result = await getJobByJobId(internalId, jobId);
    if (!result) {
      return errorBuilder(res, 404, 'A job with the specified job ID was not found.');
    }
    // const serializedMealPlan = serializeMealPlan(result, osuId, query);

    return res.send(result);
  } catch (err) {
    return errorHandler(res, err);
  }
};

const patch = () => {
  // todo
};

export { get, patch };
