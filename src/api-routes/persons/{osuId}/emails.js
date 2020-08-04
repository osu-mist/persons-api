import { getEmailsByOsuId, createEmail, preferredEmailExists } from 'db/oracledb/emails-dao';
import { personExists } from 'db/oracledb/persons-dao';
import { errorHandler, errorBuilder } from 'errors/errors';
import { serializeEmails, serializeEmail } from 'serializers/emails-serializer';

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

/**
 * Post email address
 *
 * @type {RequestHandler}
 */
const post = async (req, res) => {
  try {
    const { body: { data: { attributes } }, params: { osuId } } = req;

    const internalId = await personExists(osuId);
    if (!internalId) {
      return errorBuilder(res, 404, 'A person with the specified OSU ID was not found.');
    }

    if (attributes.preferredInd) {
      const preferredEmailId = await preferredEmailExists(internalId);
      if (preferredEmailId) {
        return errorBuilder(
          res,
          409,
          `A preferred email already exists with emailId ${preferredEmailId}`,
        );
      }
    }

    const result = await createEmail(internalId, attributes);
    const serializedEmail = await serializeEmail(result, osuId);
    return res.send(serializedEmail);
  } catch (err) {
    return errorHandler(res, err);
  }
};

export { get, post };
