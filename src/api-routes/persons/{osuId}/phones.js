import { errorHandler, errorBuilder } from 'errors/errors';
import { personExists } from 'db/oracledb/persons-dao';
import { getPhones } from 'db/oracledb/phones-dao';
import { serializePhones } from 'serializers/phones-serializer';

/**
 * Get phones by OSU ID
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

    const results = await getPhones(internalId, query);
    const serializedPhones = serializePhones(results, osuId, query);
    return res.send(serializedPhones);
  } catch (err) {
    return errorHandler(res, err);
  }
};

/**
 * Post phones endpoint
 *
 * @type {RequestHandler}
 */
const post = async () => {
  // todo
};

export { get, post };
