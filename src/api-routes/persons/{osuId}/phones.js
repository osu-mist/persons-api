import { errorHandler } from 'errors/errors';
import { getPhones } from 'db/oracledb/phones-dao';

/**
 * Get phones by OSU ID
 *
 * @type {RequestHandler}
 */
const get = async (req, res) => {
  try {
    console.log('get phones endpoint');
    const { osuId } = req.params;
    const { query } = req;
    const result = await getPhones(osuId, query);
    return res.send(result);
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
