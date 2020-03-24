import { errorHandler } from 'errors/errors';
import { getAddressesByOsuId } from 'db/oracledb/addresses-dao';

/**
 * Get addresses by OSU ID
 *
 * @type {RequestHandler}
 */
const get = async (req, res) => {
  try {
    const { osuId } = req.params;
    const { query } = req;
    const result = await getAddressesByOsuId(osuId, query);
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
