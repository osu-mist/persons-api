import _ from 'lodash';
import oracledb from 'oracledb';

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

    return rows;
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
  binds.outId = { type: oracledb.DB_TYPE_VARCHAR, dir: oracledb.BIND_OUT };
  binds.changeReasonCode = body.changeReason.code;

  const result = await connection.execute(contrib.updateJob(binds), binds);
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
  binds.outId = { type: oracledb.DB_TYPE_VARCHAR, dir: oracledb.BIND_OUT };

  return connection.execute(contrib.terminateJob(binds), binds);
};

const isValidChangeReasonCode = async (connection, changeReasonCode) => {
  const { rows } = await connection.execute(
    contrib.validateChangeReasonCode(),
    { changeReasonCode },
  );
  return rows[0].count > 0;
};

const createOrUpdateJob = async (update, osuId, body) => {
  const connection = await getConnection();
  try {
    const { changeReason: { code: changeReasonCode } } = body;

    if (_.includes(['TERME', 'TERMJ'], changeReasonCode)) {
      console.log('termination');
      const result = await terminateJob(connection, osuId, body);
      console.log(result);
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
    return undefined;
  } finally {
    connection.close();
  }
};

export { getJobs, createOrUpdateJob };
