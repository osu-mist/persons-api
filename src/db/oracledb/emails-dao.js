import { parseQuery } from 'utils/parse-query';
import { getConnection } from './connection';
import { contrib } from './contrib/contrib';

/**
 * Query data source for email data
 *
 * @param {string} osuId OSU ID of a person
 * @param {*} query Query parameters passed in with request
 * @returns {object[]} raw email data
 */
const getEmailsByOsuId = async (osuId, query) => {
  const connection = await getConnection();
  try {
    const parsedQuery = parseQuery(query);
    const { rows } = await connection.execute(contrib.getEmailsByOsuId(parsedQuery), { osuId });
    return rows;
  } finally {
    connection.close();
  }
};

export { getEmailsByOsuId };
