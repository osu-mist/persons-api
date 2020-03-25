import { serializePhones } from 'serializers/phones-serializer';
import { errorHandler } from 'errors/errors';
import { getPhones } from 'db/oracledb/phones-dao';

/**
 * Get phones by OSU ID
 *
 * @type {RequestHandler}
 */
const get = async (req, res) => {
  try {
    const { query, params: { osuId } } = req;
    const results = await getPhones(osuId, query);
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
