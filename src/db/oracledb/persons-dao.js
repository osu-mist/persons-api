import _ from 'lodash';

import { getConnection } from './connection';
import { contrib } from './contrib/contrib';
import { serializePerson } from '../../serializers/persons-serializer';

/**
 * Queries data source for raw person data and passes it to the serializer
 *
 * @param {object} binds object
 * @returns {Promise<object>} Serialized person resource from person-serializer
 */
const getPersonById = async (binds) => {
  const connection = await getConnection();
  try {
    const { rows } = await connection.execute(contrib.getPersonById(), binds);

    if (rows.length > 1) {
      throw new Error('Expect a single object but got multiple results.');
    } else if (_.isEmpty(rows)) {
      return null;
    }

    return serializePerson(rows[0]);
  } finally {
    connection.close();
  }
};

export {
  getPersonById,
};
