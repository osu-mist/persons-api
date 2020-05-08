import { personExists } from 'db/oracledb/persons-dao';
import { createSsn, ssnIsNotNull } from 'db/oracledb/ssn-dao';
import { errorHandler, errorBuilder } from 'errors/errors';

const post = async (req, res) => {
  try {
    const { body: { data: { attributes } }, params: { osuId } } = req;

    const internalId = await personExists(osuId);
    if (!internalId) {
      return errorBuilder(res, 404, 'A person with the specified OSU ID was not found.');
    }

    if (await ssnIsNotNull(osuId)) {
      return errorBuilder(res, 400, ["Person's SSN is not null or in vault"]);
    }

    const result = await createSsn(internalId, attributes);

    return res.send(result);
  } catch (err) {
    return errorHandler(res, err);
  }
};

export { post };
