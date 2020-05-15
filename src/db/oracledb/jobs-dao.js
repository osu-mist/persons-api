/* eslint-disable no-unused-vars */
import _ from 'async-dash';
import { flatten } from 'flat';
import oracledb from 'oracledb';

import { parseQuery } from 'utils/parse-query';
import { getConnection } from './connection';
import { contrib } from './contrib/contrib';

const studentBinds = [
  'status_code',
  'hourlyRate',
  'timesheet_current_code',
  'appointmentPercent',
  'jobDescription',
  'campus_code',
  'personnelChangeDate',
  'hoursPerPay',
  'salary_paysPerYear',
  'salary_annual',
  'strsAssignment_code',
  'fullTimeEquivalency',
  'earningCode_effectiveDate',
  'earningCode_code',
  'earningCode_hours',
  'supervisor_positionNumber',
  'supervisor_suffix',
  'supervisor_osuId',
  'beginDate',
  'accruesLeaveInd',
  'contractBeginDate',
  'contractEndDate',
  'useTemporarySsnInd',
  'employeeInformationReleaseInd',
  'retirement_code',
  'i9Form_code',
  'i9Form_date',
  'i9Form_expirationDate',
];

const graduateBinds = [
  'personnelChangeDate',
  'status_code',
  'hourlyRate',
  'timesheet_current_code',
  'appointmentPercent',
  'jobDescription',
  'campus_code',
  'hoursPerPay',
  'salary_paysPerYear',
  'salary_annual',
  'strsAssignment_code',
  'fullTimeEquivalency',
  'earningCode_effectiveDate',
  'earningCode_code',
  'earningCode_hours',
  'supervisor_positionNumber',
  'supervisor_suffix',
  'supervisor_osuId',
  'beginDate',
  'accruesLeaveInd',
  'contractBeginDate',
  'contractEndDate',
  'useTemporarySsnInd',
  'employeeInformationReleaseInd',
  'salaryInformationReleaseInd',
  'salaryInformationReleaseDate',
  'i9Form_code',
  'i9Form_date',
  'i9Form_expirationDate',
];

/**
 * Gets labor distribution data for each job passed in
 *
 * @param {object} connection oracledb connection
 * @param {string} internalId Internal ID of a person
 * @param {object[]} jobs raw job records
 */
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

/**
 * Queries for a specific job record belonging to a person
 *
 * @param {object} connection oracledb connection
 * @param {string} internalId Internal ID of a person
 * @param {string} jobId ID of a job record
 * @returns {object} Single raw job record
 */
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

/**
 * Creates oracledb connection object and calls getJobByJobIdWithConnection
 *
 * @param {string} internalId Internal ID of a person
 * @param {string} jobId ID of a job record
 * @returns {object} Single raw job record
 */
const getJobByJobId = async (internalId, jobId) => {
  const connection = await getConnection('banner');
  try {
    return await getJobByJobIdWithConnection(connection, internalId, jobId);
  } finally {
    connection.close();
  }
};

/**
 * Updates job record
 *
 * @param {object} connection oracledb connection
 * @param {string} osuId OSU ID of a person
 * @param {object} body Request body
 * @returns {string} Query result, null if success
 */
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
  binds.annualSalary = body.salary.annual;

  const { outBinds: { result } } = await connection.execute(contrib.updateJob(binds), binds);
  return result;
};

const flattenBody = (body) => flatten(body, { delimiter: '_' });

/**
 * Multiple labor distributions can be created/updated at one time by concatenating the fields
 * using '|' as the delimiter
 *
 * @param {object} body Request body
 */
const formatLaborDistributionForDb = (body) => {
  const laborFields = [
    { bind: 'laborEffectiveDates', attribute: 'effectiveDate' },
    { bind: 'laborAccountIndexCodes', attribute: 'accountIndex' },
    { bind: 'laborFundCodes', attribute: 'fund' },
    { bind: 'laborOrganizationCodes', attribute: 'organization' },
    { bind: 'laborAccountCodes', attribute: 'account' },
    { bind: 'laborProgramCodes', attribute: 'program' },
    { bind: 'laborActivityCodes', attribute: 'activity' },
    { bind: 'laborLocationCodes', attribute: 'location' },
    { bind: 'laborDistributionPercentages', attribute: 'distributionPercent' },
  ];

  body.laborCount = body.laborDistribution.length;
  _.forEach(body.laborDistribution, (laborDist) => {
    _.forOwn(laborFields, ({ bind, attribute }) => {
      if (body[bind]) {
        body[bind] += `|${laborDist[attribute]}`;
      } else {
        body[bind] = laborDist[attribute];
      }
    });
  });
};

/**
 * Transforms body into binds ready to be used with a sql execution
 *
 * @param {string} osuId OSU ID of a person
 * @param {object} body Request body
 * @param {string[]} additionalFields Extra fields to add to binds that aren't normally included
 * @returns {object} sql binds to be used with a sql execution
 */
const standardBinds = (osuId, body, additionalFields) => {
  if (_.includes(additionalFields, 'laborDistribution') && body.laborDistribution !== undefined) {
    formatLaborDistributionForDb(body);
  }

  const flattenedBody = flattenBody(body);

  if (_.includes(additionalFields, 'accruesLeaveInd') && flattenedBody.accruesLeaveInd !== undefined) {
    flattenedBody.accruesLeaveInd = flattenedBody.accruesLeaveInd ? 'Y' : 'N';
  }
  if (_.includes(additionalFields, 'useTemporarySsnInd') && flattenedBody.useTemporarySsnInd !== undefined) {
    flattenedBody.useTemporarySsnInd = flattenedBody.useTemporarySsnInd.toString();
  }
  if (_.includes(additionalFields, 'employeeInformationReleaseInd')
      && flattenedBody.employeeInformationReleaseInd !== undefined) {
    flattenedBody.employeeInformationReleaseInd = flattenedBody
      .employeeInformationReleaseInd
      .toString();
  }
  if (_.includes(additionalFields, 'salaryInformationReleaseInd')
      && flattenedBody.salaryInformationReleaseInd !== undefined) {
    flattenedBody.salaryInformationReleaseInd = flattenedBody
      .salaryInformationReleaseInd
      .toString();
  }

  const binds = _.pick(flattenedBody, [
    'positionNumber',
    'suffix',
    'effectiveDate',
    'changeReason_code',
    ...additionalFields || [],
  ]);
  binds.osuId = osuId;
  // binds.changeReasonCode = body.changeReason.code;
  binds.result = { type: oracledb.DB_TYPE_VARCHAR, dir: oracledb.BIND_OUT };

  return binds;
};

/**
 * Terminates a job record
 *
 * @param {object} connection oracledb connection
 * @param {string} osuId OSU ID of a person
 * @param {object} body Request body
 * @returns {string} Query result, null if success
 */
const terminateJob = async (connection, osuId, body) => {
  const binds = standardBinds(osuId, body);

  const { outBinds: { result } } = await connection.execute(contrib.terminateJob(), binds);
  return result;
};

/**
 * Updates labor distribution for a job
 *
 * @param {object} connection oracledb connection
 * @param {string} osuId OSU ID of a person
 * @param {object} body Request body
 * @returns {string} Query result, null if success
 */
const updateLaborChangeJob = async (connection, osuId, body) => {
  const binds = standardBinds(osuId, body, ['laborDistribution']);
};

/**
 * Updates student job records
 *
 * @param {object} connection oracledb connection
 * @param {string} osuId OSU ID of a person
 * @param {object} body Request body
 * @returns {string} Query result, null if success
 */
const studentUpdateJob = async (connection, osuId, body) => {
  const binds = standardBinds(osuId, body, studentBinds);

  const result = await connection.execute(contrib.studentUpdateJob(binds), binds);
  return result.outBinds.result;
};

/**
 * Creates student job records
 *
 * @param {object} connection oracledb connection
 * @param {string} osuId OSU ID of a person
 * @param {object} body Request body
 * @returns {string} Query result, null if success
 */
const studentCreateJob = async (connection, osuId, body) => {
  const binds = standardBinds(osuId, body, studentBinds);

  const { outBinds: { result } } = await connection.execute(contrib.studentCreateJob(binds), binds);
  return result;
};

/**
 * Creates graduate job records
 *
 * @param {object} connection oracledb connection
 * @param {string} osuId OSU ID of a person
 * @param {object} body Request body
 * @returns {string} Query result, null if success
 */
const graduateCreateJob = async (connection, osuId, body) => {
  const binds = standardBinds(osuId, body, graduateBinds);
  console.log(binds);

  const { outBinds: { result } } = await connection.execute(
    contrib.graduateCreateJob(binds),
    binds,
  );
  return result;
};

/**
 * Updates graduate job records
 *
 * @param {object} connection oracledb connection
 * @param {string} osuId OSU ID of a person
 * @param {object} body Request body
 * @returns {string} Query result, null if success
 */
const graduateUpdateJob = async (connection, osuId, body) => {
  const binds = standardBinds(osuId, body, graduateBinds);

  const { outBinds: { result } } = await connection.execute(
    contrib.graduateUpdateJob(binds),
    binds,
  );
  return result;
};

/**
 * Determines if changeReasonCode is valid
 *
 * @param {object} connection oracledb connection
 * @param {string} changeReasonCode Change reason code from request body
 * @returns {boolean} True if changeReasonCode is valid
 */
const isValidChangeReasonCode = async (connection, changeReasonCode) => {
  const { rows } = await connection.execute(
    contrib.validateChangeReasonCode(),
    { changeReasonCode },
  );
  return rows[0].count > 0;
};

/**
 * Determines which Epaf to execute based on fields in body
 *
 * @param {boolean} update True if updating record. False if creating record
 * @param {string} osuId OSU ID of a person
 * @param {object} body Request body
 * @param {string} internalId Internal ID of a person
 * @returns {object} raw job record
 */
const createOrUpdateJob = async (update, osuId, body, internalId) => {
  const connection = await getConnection('banner');
  try {
    let result;
    const { employmentType, changeReason: { code: changeReasonCode } } = body;

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
        if (changeReasonCode === 'NONE') {
          console.log('labor change');
          result = await updateLaborChangeJob(connection, osuId, body);
        } else if (changeReasonCode === 'BREAP') {
          console.log('BREAP');
          if (employmentType === 'student') {
            console.log('student');
            result = await studentUpdateJob(connection, osuId, body);
          } else if (employmentType === 'graduate') {
            console.log('graduate');
            result = await graduateUpdateJob(connection, osuId, body);
          }
        } else {
          await updateJob(connection, osuId, body);
        }
      } else {
        console.log('not update');
        if (employmentType === 'student') {
          console.log('student');
          result = await studentCreateJob(connection, osuId, body);
        } else if (employmentType === 'graduate') {
          console.log('graduate');
          result = await graduateCreateJob(connection, osuId, body);
        }
      }
    }
    // null === success
    if (!result) {
      // return getJob;
      const jobId = `${body.positionNumber}-${body.suffix}`;
      return await getJobByJobIdWithConnection(connection, internalId, jobId);
    }
    throw new Error(result);
  } finally {
    connection.close();
  }
};

export { getJobs, getJobByJobId, createOrUpdateJob };
