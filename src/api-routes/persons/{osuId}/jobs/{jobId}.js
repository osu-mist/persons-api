import { getJobByJobId, createOrUpdateJob } from 'db/oracledb/jobs-dao';
import { personExists } from 'db/oracledb/persons-dao';
import { errorHandler, errorBuilder } from 'errors/errors';
import { serializeJob } from 'serializers/jobs-serializer';

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
    const serializedJob = serializeJob(result, osuId);

    return res.send(serializedJob);
  } catch (err) {
    return errorHandler(res, err);
  }
};

/**
 * Patch jobs endpoint
 *
 * @type {RequestHandler}
 */
const patch = async (req, res) => {
  try {
    const { body, params: { osuId, jobId } } = req;

    const internalId = await personExists(osuId);
    if (!internalId) {
      return errorBuilder(res, 404, 'A person with the specified OSU ID was not found.');
    }

    const jobExists = await getJobByJobId(internalId, jobId);
    if (!jobExists) {
      return errorBuilder(res, 404, 'A job with the specified job ID was not found.');
    }

    const result = await createOrUpdateJob(true, osuId, body.data.attributes, internalId);

    if (result instanceof Error) {
      return errorBuilder(res, 400, [result.message]);
    }

    const serializedJob = serializeJob(result, osuId);
    return res.send(serializedJob);
  } catch (err) {
    return errorHandler(res, err);
  }
};

export { get, patch };
