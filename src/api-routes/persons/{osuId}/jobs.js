import { getJobs, getJobByJobId, createOrUpdateJob } from 'db/oracledb/jobs-dao';
import { personExists } from 'db/oracledb/persons-dao';
import { errorHandler, errorBuilder } from 'errors/errors';
import { serializeJobs, serializePostOrPatch } from 'serializers/jobs-serializer';

/**
 * Get jobs by OSU ID
 *
 * @type {RequestHandler}
 */
const get = async (req, res) => {
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
const post = async (req, res) => {
  try {
    const { body, params: { osuId } } = req;
    const {
      data: {
        attributes: {
          positionNumber,
          suffix,
          changeReason: {
            code: changeReasonCode,
          },
        },
      },
    } = body;

    const internalId = await personExists(osuId);
    if (!internalId) {
      return errorBuilder(res, 404, 'A person with the specified OSU ID was not found.');
    }

    const job = await getJobByJobId(internalId, `${positionNumber}-${suffix}`);
    if (job) {
      return errorBuilder(res, 409, 'A job with the specified job ID already exists.');
    }

    if (changeReasonCode !== 'AAHIR') {
      return errorBuilder(res, 400, ['AAHIR change reason code must be used to create job records.']);
    }

    const result = await createOrUpdateJob('create', osuId, body.data.attributes);

    if (result instanceof Error) {
      return errorBuilder(res, 400, [result.message]);
    }

    const serializedJob = serializePostOrPatch(osuId, body);
    return res.status(202).send(serializedJob);
  } catch (err) {
    return errorHandler(res, err);
  }
};

export { get, post };
