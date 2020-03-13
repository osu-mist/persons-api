import _ from 'lodash';

import { serializePerson } from 'serializers/persons-serializer';
import { getConnection } from './connection';
import { contrib } from './contrib/contrib';

/**
 * Queries data source for raw person data and passes it to the serializer
 *
 * @param {string} osuId OSU ID of person to select
 * @returns {Promise<object>} Serialized person resource from person-serializer
 */
const getPersonById = async (osuId) => {
  const connection = await getConnection();
  try {
    const query = { osuId };
    const { rows } = await connection.execute(contrib.getPersonById(), query);

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
