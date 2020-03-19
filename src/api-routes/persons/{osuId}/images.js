import { errorHandler, errorBuilder } from 'errors/errors';
// import { getPersonById } from 'db/oracledb/persons-dao';
import { getImageById } from 'db/oracledb/images-dao';

/**
 * Get person by ID
 *
 * @type {RequestHandler}
 */
const get = async (req, res) => {
  try {
    const { osuId } = req.params;
    const result = await getImageById(osuId);

    if (!result) {
      return errorBuilder(res, 404, 'A person with the specified OSU ID was not found.');
    }

    res.contentType('image/jpeg');
    return res.send(result);
  } catch (err) {
    return errorHandler(res, err);
  }
};

export { get };
