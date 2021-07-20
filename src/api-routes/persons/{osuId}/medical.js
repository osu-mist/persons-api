import { getMedical } from 'db/oracledb/medical-dao';
import { personExists } from 'db/oracledb/persons-dao';
import { errorHandler, errorBuilder } from 'errors/errors';
import { serializeMedical } from 'serializers/medical-serializer';

/**
 * Get medical records by OSU ID
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

    const results = await getMedical(internalId, query);

    return res.send(serializeMedical(results, osuId, query));
  } catch (err) {
    return errorHandler(res, err);
  }
};

export { get };
