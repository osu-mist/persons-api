import { getEmailsByOsuId } from 'db/oracledb/emails-dao';
import { errorHandler } from 'errors/errors';
import { serializeEmails } from 'serializers/emails-serializer';

/**
 * Get emails of a person by OSU ID
 *
 * @type {RequestHandler}
 */
const get = async (req, res) => {
  try {
    const { query, params: { osuId } } = req;
    const result = await getEmailsByOsuId(osuId, query);
    const serializedEmails = serializeEmails(result, osuId, query);
    return res.send(serializedEmails);
  } catch (err) {
    return errorHandler(res, err);
  }
};

export { get };
