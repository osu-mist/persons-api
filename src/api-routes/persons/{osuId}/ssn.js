import { personExists } from 'db/oracledb/persons-dao';
import { createSsn, ssnIsNotNull } from 'db/oracledb/ssn-dao';
import { errorHandler, errorBuilder } from 'errors/errors';

/**
 * Post SSN endpoint
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

    if (await ssnIsNotNull(internalId)) {
      return errorBuilder(res, 400, ["Person's SSN is not null or in vault"]);
    }

    await createSsn(internalId, attributes);

    return res.sendStatus(204);
  } catch (err) {
    return errorHandler(res, err);
  }
};

export { post };
