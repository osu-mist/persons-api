import { createPerson } from 'db/oracledb/persons-dao';
import { errorHandler } from 'errors/errors';
import { serializePerson } from 'serializers/persons-serializer';

/**
 * Post person endpoint
 *
 * @type {RequestHandler}
 */
const post = async (req, res) => {
  try {
    const { body: { data: { attributes } } } = req;

    const result = await createPerson(attributes);
    const serializedPerson = serializePerson(result);
    return res.send(serializedPerson);
  } catch (err) {
    return errorHandler(res, err);
  }
};

export { post };
