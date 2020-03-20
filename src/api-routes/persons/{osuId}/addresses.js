/* eslint-disable no-unused-vars */
import { errorHandler } from 'errors/errors';

/**
 * Get addresses by OSU ID
 *
 * @type {RequestHandler}
 */
const get = async (req, res) => {
  console.log('get addresses endpoint');
  try {
    // const { osuId } = req.params;
    const result = null;
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
