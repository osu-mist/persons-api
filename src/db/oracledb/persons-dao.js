import _ from 'lodash';

import { serializePerson } from 'serializers/persons-serializer';
import { getConnection } from './connection';
import { contrib } from './contrib/contrib';

/**
 * Returns true if person with the given OSU ID exists
 *
 * @param {string} osuId OSU ID of a person
 * @returns {boolean} true if person exists
 */
const personExists = async (osuId) => {
  const connection = await getConnection();
  try {
    const { rows } = await connection.execute(contrib.personExists(), { osuId });
    if (rows.length > 0) {
      return rows[0].internalId;
    }

    return null;
  } finally {
    connection.close();
  }
};

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
      return undefined;
    }

    return serializePerson(rows[0]);
  } finally {
    connection.close();
  }
};

export { getPersonById, personExists };
