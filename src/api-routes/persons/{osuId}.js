import { errorHandler, errorBuilder } from 'errors/errors';
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
    const { osuId } = req.params;
    const result = await getPersonById(osuId);
    if (!result) {
      return errorBuilder(res, 404, 'A person with the specified osu ID was not found.');
    }
    return res.send(result);
  } catch (err) {
    return errorHandler(res, err);
  }
};

export { get };
