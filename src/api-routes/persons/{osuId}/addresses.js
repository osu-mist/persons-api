import { errorHandler, errorBuilder } from 'errors/errors';
import { getAddressesById } from 'db/oracledb/addresses-dao';

/**
 * Get addresses by OSU ID
 *
 * @type {RequestHandler}
 */
const get = async (req, res) => {
  try {
    const { osuId } = req.params;
    const { query } = req;
    const result = await getAddressesById(osuId, query);
    if (!result) {
      return errorBuilder(res, 404, 'No address records were found for the specified OSU ID');
    }
    return res.send(result);
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
