import { getAddressesByInternalId, createAddress } from 'db/oracledb/addresses-dao';
import { personExists } from 'db/oracledb/persons-dao';
import { errorHandler, errorBuilder } from 'errors/errors';
import { serializeAddress, serializeAddresses } from 'serializers/addresses-serializer';

/**
 * Get addresses by OSU ID
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

    const result = await getAddressesByInternalId(internalId, query);
    const serializedAddresses = serializeAddresses(result, query, osuId);
    return res.send(serializedAddresses);
  } catch (err) {
    return errorHandler(res, err);
  }
};

/**
 * Post address
 *
 * @type {RequestHandler}
 */
const post = async (req, res) => {
  try {
    const { body, params: { osuId } } = req;

    const internalId = await personExists(osuId);
    if (!internalId) {
      return errorBuilder(res, 404, 'A person with the specified OSU ID was not found.');
    }

    const result = await createAddress(internalId, body.data.attributes);
    // need to make serializeAddress method
    const serializedAddress = serializeAddress(result, osuId);
    return res.send(serializedAddress);
  } catch (err) {
    return errorHandler(res, err);
  }
};

export { get, post };
