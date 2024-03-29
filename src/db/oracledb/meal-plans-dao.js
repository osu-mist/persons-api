import _ from 'lodash';

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
  const connection = await getConnection('ods');
  try {
    const parsedQuery = parseQuery(query);
    parsedQuery.osuId = osuId;

    // create query string
    const getMealPlansQuery = contrib.getMealPlansByOsuId(parsedQuery);
    // strip operators from parsedQuery so they are not passed into the query execution
    _.forEach(parsedQuery, (value, param) => {
      if (value.operator) {
        parsedQuery[param] = value.value;
      }
    });
    const { rows } = await connection.execute(getMealPlansQuery, parsedQuery);
    return rows;
  } finally {
    connection.close();
  }
};

/**
 * Queries data source for raw meal-plan data
 *
 * @param {string} osuId OSU ID of person to select addresses from
 * @param {string} mealPlanId ID of meal plan to select
 * @returns {Promise<object>} Serialized address resource
 */
const getMealPlanByMealPlanId = async (osuId, mealPlanId) => {
  const connection = await getConnection('ods');
  try {
    const query = { osuId, mealPlanId };
    const { rows } = await connection.execute(contrib.getMealPlansByOsuId(query), query);

    return rows.length < 1 ? undefined : rows[0];
  } finally {
    connection.close();
  }
};

export { getMealPlansByOsuId, getMealPlanByMealPlanId };
