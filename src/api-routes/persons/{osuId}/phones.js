import { personExists } from 'db/oracledb/persons-dao';
import { serializePhones } from 'serializers/phones-serializer';
import { errorHandler, errorBuilder } from 'errors/errors';
import { getPhones } from 'db/oracledb/phones-dao';

/**
 * Get phones by OSU ID
 *
 * @type {RequestHandler}
 */
const get = async (req, res) => {
  try {
    const { query, params: { osuId } } = req;

    const pidm = await personExists(osuId);
    if (!pidm) {
      return errorBuilder(res, 404, 'A person with the specified OSU ID was not found.');
    }

    const results = await getPhones(pidm, query);
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
