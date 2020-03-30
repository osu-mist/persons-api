import { parseQuery } from 'utils/parse-query';
import { getConnection } from './connection';
import { contrib } from './contrib/contrib';

/**
 * Queries data source for raw meal-plan data
 *
 * @param {string} osuId OSU ID of person to select addresses from
 * @param {object} query Query parameters passed in with request
 * @returns {Promise<object>} Serialized address resource
 */
const getMealPlansByOsuId = async (osuId, query) => {
  const connection = await getConnection('odsRead');
  try {
    const parsedQuery = parseQuery(query);
    parsedQuery.osuId = osuId;
    parsedQuery.balance = parsedQuery.balance.toString();

    const { rows } = await connection.execute(
      contrib.getMealPlansByOsuId(parsedQuery),
      parsedQuery,
    );
    return rows;
  } finally {
    connection.close();
  }
};

export { getMealPlansByOsuId };
