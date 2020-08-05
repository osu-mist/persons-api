import { getEmailByEmailId } from 'db/oracledb/emails-dao';
import { personExists } from 'db/oracledb/persons-dao';
import { serializeEmail } from 'serializers/emails-serializer';
import { errorHandler, errorBuilder } from 'errors/errors';

/**
 * Get email by email ID
 *
 * @type {RequestHandler}
 */
const get = async (req, res) => {
  try {
    const { params: { osuId, emailId } } = req;

    const internalId = await personExists(osuId);
    if (!internalId) {
      return errorBuilder(res, 404, 'A person with the specified OSU ID was not found.');
    }

    const result = await getEmailByEmailId(internalId, emailId);
    if (!result) {
      return errorBuilder(res, 404, 'An email with the specified email ID was not found.');
    }

    const serializedEmail = serializeEmail(result, osuId);
    return res.send(serializedEmail);
  } catch (err) {
    return errorHandler(res, err);
  }
};

export { get };
