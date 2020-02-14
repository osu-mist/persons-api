/* eslint-disable no-unused-vars */
import { errorHandler } from 'errors/errors';
import { getPerson } from '../db/oracledb/persons-dao';

/**
 * Base person endpoint
 *
 * @param {object} req request
 * @param {object} res response
 * @returns {Promise<object>} response
 */
const get = async (req, res) => {
  try {
    const { query } = req;
    // set osuId to empty array so array functions can be called
    query.osuId = query.osuId || [];
    const result = await getPerson(query);
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

export {
  get,
  post,
};
