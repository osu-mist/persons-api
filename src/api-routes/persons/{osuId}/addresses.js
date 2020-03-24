import { serializeAddresses } from 'serializers/addresses-serializer';
import { errorHandler } from 'errors/errors';
import { getAddressesByOsuId } from 'db/oracledb/addresses-dao';

/**
 * Get addresses by OSU ID
 *
 * @type {RequestHandler}
 */
const get = async (req, res) => {
  try {
    const { query, params: { osuId } } = req;
    const result = await getAddressesByOsuId(osuId, query);
    const serializedAddresses = serializeAddresses(result, query, osuId);
    return res.send(serializedAddresses);
  } catch (err) {
    return errorHandler(res, err);
  }
};

/**
 * Post address
 *
 */
const post = async () => {
  // TODO
};

export { get, post };
