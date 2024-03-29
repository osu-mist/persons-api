import { getPersonById } from 'db/oracledb/persons-dao';
import { errorHandler, errorBuilder } from 'errors/errors';
import { serializePerson } from 'serializers/persons-serializer';

/**
 * Get person by ID
 *
 * @type {RequestHandler}
 */
const get = async (req, res) => {
  try {
    const { osuId } = req.params;
    const result = await getPersonById(osuId);
    if (!result) {
      return errorBuilder(res, 404, 'A person with the specified OSU ID was not found.');
    }
    const serializedPerson = serializePerson(result);
    return res.send(serializedPerson);
  } catch (err) {
    return errorHandler(res, err);
  }
};

export { get };
