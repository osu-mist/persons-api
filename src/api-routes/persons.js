import { createPerson } from 'db/oracledb/persons-dao';
import { errorHandler } from 'errors/errors';

/**
 * Post person endpoint
 *
 * @type {RequestHandler}
 */
const post = async (req, res) => {
  try {
    const { body: { data: { attributes } } } = req;

    const result = await createPerson(attributes);
    return res.send(result);
  } catch (err) {
    return errorHandler(res, err);
  }
};

export { post };
