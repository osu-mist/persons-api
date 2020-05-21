import _ from 'async-dash';

import { parseQuery } from 'utils/parse-query';
import { getConnection } from './connection';
import { contrib } from './contrib/contrib';

const getLaborDistributions = async (connection, internalId, jobs) => {
  await _.asyncEach(jobs, async (job) => {
    const binds = await _.pick(job, ['positionNumber', 'suffix']);
    binds.internalId = internalId;
    const { rows: labors } = await connection.execute(contrib.getLaborDistribution(), binds);
    job.laborDistribution = labors;
  });
};

/**
 * Queries data source for raw person data and passes it to the serializer
 *
 * @param {string} internalId internal ID of person to select
 * @param {object} query query parameters passed in with the request
 * @returns {Promise<object>} Raw job data from data source
 */
const getJobs = async (internalId, query) => {
  const connection = await getConnection('banner');
  try {
    const parsedQuery = parseQuery(query);
    parsedQuery.internalId = internalId;
    const binds = _.omit(parsedQuery, ['studentEmployee']);

    const { rows } = await connection.execute(contrib.getJobs(parsedQuery), binds);

    await getLaborDistributions(connection, internalId, rows);

    return rows;
  } finally {
    connection.close();
  }
};

const getJobByJobId = async (internalId, jobId) => {
  const connection = await getConnection('banner');
  try {
    const [positionNumber, suffix] = jobId.split('-');
    const binds = { internalId, positionNumber, suffix };
    const { rows } = await connection.execute(contrib.getJobs(binds), binds);

    if (rows.length > 1) {
      throw new Error(`Multiple job records found for job ID ${jobId}`);
    }

    await getLaborDistributions(connection, internalId, rows);

    return rows[0];
  } finally {
    connection.close();
  }
};

export { getJobs, getJobByJobId };
