import _ from 'async-dash';
import oracledb from 'oracledb';

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
    const binds = _.omit(parsedQuery, ['employmentType']);

    const { rows } = await connection.execute(contrib.getJobs(parsedQuery), binds);

    await getLaborDistributions(connection, internalId, rows);

    return rows;
  } finally {
    connection.close();
  }
};

const getJobByJobIdWithConnection = async (connection, internalId, jobId) => {
  const [positionNumber, suffix] = jobId.split('-');
  const binds = { internalId, positionNumber, suffix };
  const { rows } = await connection.execute(contrib.getJobs(binds), binds);

  if (rows.length > 1) {
    throw new Error(`Multiple job records found for job ID ${jobId}`);
  }

  await getLaborDistributions(connection, internalId, rows);

  return rows[0];
};

const getJobByJobId = async (internalId, jobId) => {
  const connection = await getConnection('banner');
  try {
    return await getJobByJobIdWithConnection(connection, internalId, jobId);
  } finally {
    connection.close();
  }
};

const updateJob = async (connection, osuId, body) => {
  const binds = _.pick(body, [
    'effectiveDate',
    'positionNumber',
    'suffix',
    'hourlyRate',
    'appointmentPercent',
    'personnelChangeDate',
    'hoursPerPay',
    'annualSalary',
    'fullTimeEquivalency',
  ]);
  binds.osuId = osuId;
  binds.result = { type: oracledb.DB_TYPE_VARCHAR, dir: oracledb.BIND_OUT };
  binds.changeReasonCode = body.changeReason.code;

  const { outBinds: { result } } = await connection.execute(contrib.updateJob(binds), binds);
  return result;
};

const terminateJob = async (connection, osuId, body) => {
  const binds = _.pick(body, [
    'positionNumber',
    'suffix',
    'effectiveDate',
  ]);
  binds.osuId = osuId;
  binds.changeReasonCode = body.changeReason.code;
  binds.result = { type: oracledb.DB_TYPE_VARCHAR, dir: oracledb.BIND_OUT };

  const { outBinds: { result } } = await connection.execute(contrib.terminateJob(binds), binds);
  return result;
};

const isValidChangeReasonCode = async (connection, changeReasonCode) => {
  const { rows } = await connection.execute(
    contrib.validateChangeReasonCode(),
    { changeReasonCode },
  );
  return rows[0].count > 0;
};

const createOrUpdateJob = async (update, osuId, body) => {
  const connection = await getConnection('banner');
  try {
    let result;
    const { changeReason: { code: changeReasonCode } } = body;

    if (_.includes(['TERME', 'TERMJ'], changeReasonCode)) {
      console.log('termination');
      result = await terminateJob(connection, osuId, body);
    } else {
      console.log('not termination');
      if (!await isValidChangeReasonCode(connection, changeReasonCode)) {
        return new Error(`Invalid change reason code ${changeReasonCode}`);
      }

      if (update) {
        console.log('update');
        if (changeReasonCode === 'LCHNG') {
          console.log('LCHNG');
        } else if (changeReasonCode === 'BREAP') {
          console.log('BREAP');
        } else {
          await updateJob(connection, osuId, body);
        }
      } else {
        console.log('not update');
      }
    }

    // null === success
    if (!result) {
      // return getJob;
      return undefined;
    }
    return new Error('Error occurred');
  } finally {
    connection.close();
  }
};

export { getJobs, getJobByJobId, createOrUpdateJob };
