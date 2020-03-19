/* eslint-disable no-unused-vars */
import _ from 'lodash';

import { serializeJobs } from 'serializers/jobs-serializer';
import { parseQuery } from 'utils/parse-query';
import { getConnection } from './connection';
import { contrib } from './contrib/contrib';

/**
 * Queries data source for raw person data and passes it to the serializer
 *
 * @param {object} query Query parameters from request
 * @param {string} osuId OSU ID from request path
 * @returns {Promise<object>} Serialized person resource from person-serializer
 */
const getJobs = async (query, osuId) => {
  const connection = await getConnection();
  try {
    console.log('getJobs dao');
    const parsedQuery = parseQuery(query);
    parsedQuery.osuId = osuId;
    const { rows } = await connection.execute(contrib.getJobsById(parsedQuery), parsedQuery);
    console.log(rows);

    /* for await (const job of rows) {
      const laborQuery = {
        osuId,
        suffix: job.suffix,
        positionNumber: job.positionNumber,
      };
      const { rows: laborData } = await connection.execute(
        contrib.getJobLaborDistribution(),
        laborQuery,
      );
      job.laborData = laborData;
    } */

    return serializeJobs(rows, query);

    /* if (rows.length > 1) {
      throw new Error('Expect a single object but got multiple results.');
    } else if (_.isEmpty(rows)) {
      return undefined;
    } */
  } finally {
    connection.close();
  }
};

export { getJobs };
