import { getJobByJobId, handleJob } from 'db/oracledb/jobs-dao';
import { personExists } from 'db/oracledb/persons-dao';
import { errorHandler, errorBuilder } from 'errors/errors';
import { serializeJob, serializePostOrPatch } from 'serializers/jobs-serializer';

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
    const { data: { id: pathId, attributes } } = body;
    const { changeReason: { code: changeReasonCode } } = attributes;

    const internalId = await personExists(osuId);
    if (!internalId) {
      return errorBuilder(res, 404, 'A person with the specified OSU ID was not found.');
    }

    if (jobId !== pathId) {
      return errorBuilder(res, 409, 'Job Id in path does not match job Id in body');
    }

    if (changeReasonCode === 'AAHIR') {
      return errorBuilder(res, 400, ['AAHIR change reason code cannot be used to update job records']);
    }

    const [positionNumber, suffix] = jobId.split('-');
    if (attributes.positionNumber !== positionNumber || suffix !== attributes.suffix) {
      return errorBuilder(
        res,
        409,
        'positionNumber or suffix in attributes does not match job Id in body',
      );
    }

    const jobExists = await getJobByJobId(internalId, jobId);
    if (!jobExists) {
      return errorBuilder(res, 404, 'A job with the specified job ID was not found.');
    }

    const result = await handleJob(osuId, attributes);

    if (result instanceof Error) {
      return errorBuilder(res, 400, [result.message]);
    }

    const serializedJob = serializePostOrPatch(osuId, body);
    return res.status(202).send(serializedJob);
  } catch (err) {
    return errorHandler(res, err);
  }
};

export { get, patch };
