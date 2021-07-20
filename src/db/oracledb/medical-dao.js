import { parseQuery } from 'utils/parse-query';
import { getConnection } from './connection';
import { contrib } from './contrib/contrib';

/**
 * Queries data source for medical records
 *
 * @param {string} internalId internal ID of person to select
 * @param {object} query query parameters passed in with the request
 * @returns {Promise<object>} Raw job data from data source
 */
const getMedical = async (internalId, query) => {
  const connection = await getConnection('banner');
  try {
    const parsedQuery = parseQuery(query);
    const { rows } = await connection.execute(contrib.getMedical(parsedQuery), { internalId });
    return rows;
  } finally {
    connection.close();
  }
};

export { getMedical };
