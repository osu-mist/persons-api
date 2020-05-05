import { personExists } from 'db/oracledb/persons-dao';
import { getPhonesByInternalId, createPhone } from 'db/oracledb/phones-dao';
import { errorHandler, errorBuilder } from 'errors/errors';
import { serializePhone, serializePhones } from 'serializers/phones-serializer';

/**
 * Get phones by OSU ID
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

    const results = await getPhonesByInternalId(internalId, query);
    const serializedPhones = serializePhones(results, osuId, query);
    return res.send(serializedPhones);
  } catch (err) {
    return errorHandler(res, err);
  }
};

/**
 * Post phones endpoint
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

    const result = await createPhone(internalId, attributes, res);

    if (result instanceof Error) {
      return errorBuilder(res, 400, [result.message]);
    }

    const serializedPhone = serializePhone(result, osuId);
    return res.send(serializedPhone);
  } catch (err) {
    return errorHandler(res, err);
  }
};

export { get, post };
