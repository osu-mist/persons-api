/* eslint-disable no-unused-vars */
import { errorHandler } from 'errors/errors';
import { getAddressesById } from 'db/oracledb/addresses-dao';

/**
 * Get addresses by OSU ID
 *
 * @type {RequestHandler}
 */
const get = async (req, res) => {
  console.log('get addresses endpoint');
  try {
    const { osuId } = req.params;
    const { query } = req;
    const result = await getAddressesById(osuId, query);
    return res.send(result);
  } catch (err) {
    return errorHandler(res, err);
  }
};

/**
 * Post address
 *
 */
const post = async (req, res) => {
  // TODO
};

export { get, post };
