/* eslint-disable no-unused-vars */
import { errorHandler } from 'errors/errors';
import { getPersonById } from '../../db/oracledb/persons-dao';

/**
 * Get person by ID
 *
 * @param {object} req request
 * @param {object} res response
 * @returns {Promise<object>} response
 */
const get = async (req, res) => {
  try {
    console.log('GET person by ID');
    const { osuId } = req.params;
    const result = await getPersonById(osuId);
    return res.send(result);
  } catch (err) {
    return errorHandler(res, err);
  }
};

export { get };
