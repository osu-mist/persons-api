import _ from 'async-dash';

import { parseQuery } from 'utils/parse-query';
import { getConnection } from './connection';
import { contrib } from './contrib/contrib';

/**
 * Queries data source for raw person data and passes it to the serializer
 *
 * @param {string} internalId internal ID of person to select
 * @param {object} query query parameters passed in with the request
 * @returns {Promise<object>} Raw job data from data source
 */
const getJobs = async (internalId, query) => {
  const connection = await getConnection();
  try {
    const parsedQuery = parseQuery(query);
    parsedQuery.internalId = internalId;

    const { rows } = await connection.execute(contrib.getJobs(parsedQuery), parsedQuery);

    await _.asyncEach(rows, async (row) => {
      const binds = await _.pick(row, ['positionNumber', 'suffix']);
      binds.internalId = internalId;
      const { rows: labors } = await connection.execute(contrib.getLaborDistribution(), binds);
      row.laborDistribution = labors;
    });

    return rows;
  } finally {
    connection.close();
  }
};

export { getJobs };
