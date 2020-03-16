/* eslint-disable no-unused-vars */
import { errorHandler, errorBuilder } from 'errors/errors';
// import { getPersonById } from '../../db/oracledb/jobs-dao';

/**
 * Get jobs by ID
 *
 * @type {RequestHandler}
 */
const get = async (req, res) => {
  try {
    console.log('get jobs endpoint');
    const result = undefined;
    return res.send(result);
  } catch (err) {
    return errorHandler(res, err);
  }
};

/**
 * Post person endpoint
 *
 * @param {object} req request
 * @param {object} res response
 * @returns {Promise<object>} response
 */
const post = async (req, res) => {
  // todo
};

export { get, post };
