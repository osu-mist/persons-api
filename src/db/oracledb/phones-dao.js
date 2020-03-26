import { parseQuery } from 'utils/parse-query';
import { getConnection } from './connection';
import { contrib } from './contrib/contrib';

/**
 * Queries data source for raw person data and passes it to the serializer
 *
 * @param {string} osuId OSU ID of person to select
 * @param {object} query query parameters passed in with the request
 * @returns {Promise<object>} Raw phone data from data source
 */
const getPhones = async (osuId, query) => {
  const connection = await getConnection();
  try {
    const parsedQuery = parseQuery(query);
    parsedQuery.osuId = osuId;
    const { rows } = await connection.execute(contrib.getPhones(parsedQuery), parsedQuery);

    return rows;
  } finally {
    connection.close();
  }
};

export { getPhones };