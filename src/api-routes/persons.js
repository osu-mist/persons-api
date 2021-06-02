import moment from 'moment';

import { createPerson } from 'db/oracledb/persons-dao';
import { errorHandler, errorBuilder } from 'errors/errors';
import { serializePerson } from 'serializers/persons-serializer';

/**
 * Post person endpoint
 *
 * @type {RequestHandler}
 */
const post = async (req, res) => {
  try {
    const { body: { data: { attributes } } } = req;

    if (moment().isBefore(attributes.birthDate)) {
      return errorBuilder(res, 400, ["birthDate can't be a future date"]);
    }

    const result = await createPerson(attributes);

    if (result instanceof Error) {
      return errorBuilder(res, 400, [result.message]);
    }

    const serializedPerson = serializePerson(result);
    return res.status(202).send(serializedPerson);
  } catch (err) {
    return errorHandler(res, err);
  }
};

export { post };
