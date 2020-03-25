import { errorHandler } from 'errors/errors';
import { getEmailsByOsuId } from 'db/oracledb/emails-dao';

/**
 * Get emails of a person by OSU ID
 *
 * @type {RequestHandler}
 */
const get = async (req, res) => {
  console.log('get emails endpoint');
  try {
    const { query, params: { osuId } } = req;
    const result = await getEmailsByOsuId(osuId, query);
    return res.send(result);
  } catch (err) {
    return errorHandler(res, err);
  }
};

export { get };
