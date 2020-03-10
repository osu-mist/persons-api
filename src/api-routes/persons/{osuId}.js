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
    // const { query } = req;
    // const result = await getPersonById(query);
    // return res.send(result);
    return 'crap';
  } catch (err) {
    return errorHandler(res, err);
  }
};

export { get };
