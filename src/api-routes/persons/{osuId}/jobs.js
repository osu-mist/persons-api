import { errorHandler, errorBuilder } from 'errors/errors';
import { personExists } from 'db/oracledb/persons-dao';
import { getJobs, createOrUpdateJob } from 'db/oracledb/jobs-dao';
import { serializeJobs, getSerializerArgs } from 'serializers/jobs-serializer';

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

    const internalId = await personExists(osuId);
    if (!internalId) {
      return errorBuilder(res, 404, 'A person with the specified OSU ID was not found.');
    }

    const result = await createOrUpdateJob(false, osuId, body.data.attributes, internalId);

    if (result instanceof Error) {
      return errorBuilder(res, 400, [result.message]);
    }

    const { topLevelSelfLink } = getSerializerArgs(osuId);
    const links = { self: `${topLevelSelfLink}/${body.data.id}` };
    body.links = links;
    body.data.links = links;
    return res.send(body);
  } catch (err) {
    return errorHandler(res, err);
  }
};

export { get, post };
