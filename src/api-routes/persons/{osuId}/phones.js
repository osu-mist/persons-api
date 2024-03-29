import { personExists } from 'db/oracledb/persons-dao';
import { getPhonesByInternalId } from 'db/oracledb/phones-dao';
import { errorHandler, errorBuilder } from 'errors/errors';
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

    const results = await getPhonesByInternalId(internalId, query);
    const serializedPhones = serializePhones(results, osuId, query);
    return res.send(serializedPhones);
  } catch (err) {
    return errorHandler(res, err);
  }
};

export { get };
