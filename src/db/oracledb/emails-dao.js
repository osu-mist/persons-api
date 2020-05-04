import { parseQuery } from 'utils/parse-query';
import { getConnection } from './connection';
import { contrib } from './contrib/contrib';

/**
 * Query data source for email data
 *
 * @param {string} internalId Internal ID of a person
 * @param {*} query Query parameters passed in with request
 * @returns {object[]} raw email data
 */
const getEmailsByOsuId = async (internalId, query) => {
  const connection = await getConnection('banner');
  try {
    const parsedQuery = parseQuery(query);
    const { rows } = await connection.execute(
      contrib.getEmailsByOsuId(parsedQuery),
      { internalId },
    );
    return rows;
  } finally {
    connection.close();
  }
};

export { getEmailsByOsuId };
