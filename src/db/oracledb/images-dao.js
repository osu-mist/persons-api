import _ from 'lodash';

import { getConnection } from './connection';
import { contrib } from './contrib/contrib';

/**
 * Queries data source for raw person data and passes it to the serializer
 *
 * @param {string} osuId OSU ID of person to select
 * @returns {Promise<object>} Serialized person resource from person-serializer
 */
const getImageById = async (osuId) => {
  const connection = await getConnection();
  try {
    const query = { osuId };
    const { rows } = await connection.execute(contrib.getImageById(), query);

    if (!rows || _.isEmpty(rows)) {
      return undefined;
    }

    return rows[0].image;
  } finally {
    connection.close();
  }
};

export { getImageById };
