import { getEmailsByOsuId } from 'db/oracledb/emails-dao';
import { personExists } from 'db/oracledb/persons-dao';
import { errorHandler, errorBuilder } from 'errors/errors';
import { serializeEmails } from 'serializers/emails-serializer';

/**
 * Get emails of a person by OSU ID
 *
 * @type {RequestHandler}
 */
const get = async (req, res) => {
  try {
    const { query, params: { osuId } } = req;

    const internalId = await personExists(osuId);
    if (!internalId) {
      return errorBuilder(res, 404, 'A person with the specified OSU ID was not found.');
    }

    const result = await getEmailsByOsuId(internalId, query);
    const serializedEmails = serializeEmails(result, osuId, query);
    return res.send(serializedEmails);
  } catch (err) {
    return errorHandler(res, err);
  }
};

export { get };
