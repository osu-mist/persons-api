import _ from 'lodash';

import { parseQuery } from 'utils/parse-query';

import { getConnection } from './connection';
import { contrib } from './contrib/contrib';

/**
 * Queries phone data using passed in oracledb connection
 *
 * @param {object} connection
 * @param {string} internalId
 * @param {object} query
 * @returns {Promise<object[]>} raw phone data
 */
const getPhones = async (connection, internalId, query) => {
  const parsedQuery = parseQuery(query);
  parsedQuery.internalId = internalId;

  const omittedQuery = _.omit(parsedQuery, ['primaryInd']);
  const { rows } = await connection.execute(contrib.getPhones(parsedQuery), omittedQuery);

  return rows;
};

/**
 * Queries data source for raw person data and passes it to the serializer
 *
 * @param {string} internalId internal ID of person to select
 * @param {object} query query parameters passed in with the request
 * @returns {Promise<object>} Raw phone data from data source
 */
const getPhonesByInternalId = async (internalId, query) => {
  const connection = await getConnection('banner');
  try {
    return await getPhones(connection, internalId, query);
  } finally {
    connection.close();
  }
};

export { getPhonesByInternalId };
