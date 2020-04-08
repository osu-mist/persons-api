import { errorHandler, errorBuilder } from 'errors/errors';
import { personExists } from 'db/oracledb/persons-dao';
import { getJobs } from 'db/oracledb/jobs-dao';
import { serializeJobs } from 'serializers/jobs-serializer';

/**
 * Get jobs by OSU ID
 *
 * @type {RequestHandler}
 */
const get = async (req, res) => {
  console.log('get jobs endpoint');
  try {
    const { query, params: { osuId } } = req;

    const internalId = await personExists(osuId);
    if (!internalId) {
      return errorBuilder(res, 404, 'A person with the specified OSU ID was not found.');
    }

    const results = await getJobs(internalId, query);
    const serializedJobs = serializeJobs(results, osuId, query);
    return res.send(serializedJobs);
  } catch (err) {
    return errorHandler(res, err);
  }
};

/**
 * Post jobs endpoint
 *
 * @type {RequestHandler}
 */
const post = async () => {
  // todo
};

export { get, post };
