/* eslint-disable no-unused-vars */
import _ from 'async-dash';
import { flatten } from 'flat';
import moment from 'moment';
import oracledb from 'oracledb';

import { parseQuery } from 'utils/parse-query';
import { getConnection } from './connection';
import { contrib } from './contrib/contrib';

const studentBinds = [
  'changeReason_code',
  'employeeClassification_code',
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
  'laborDistribution',
  'homeOrganization_current_code',
  'employeeGroup_code',
];

const graduateBinds = [
  'changeReason_code',
  'employeeClassification_code',
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
  'laborDistribution',
  'homeOrganization_current_code',
  'employeeGroup_code',
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

const flattenBody = (body) => flatten(body, { delimiter: '_' });

/**
 * Multiple labor distributions can be created/updated at one time by concatenating the fields
 * using '|' as the delimiter
 *
 * @param {object} body Request body
 * @param {object} binds query binds to be passed in with connection.execute()
 */
const formatLaborDistributionForDb = (body, binds) => {
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

  binds.laborCount = body.laborDistribution.length;
  _.forEach(body.laborDistribution, (laborDist) => {
    _.forOwn(laborFields, ({ bind, attribute }) => {
      laborDist.effectiveDate = moment(laborDist.effectiveDate, 'YYYY-MM-DD').format('DD-MMM-YY');
      binds[bind] = `${binds[bind] || ''}${laborDist[attribute] || ''}|`;
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
  const binds = {
    osuId,
    result: { type: oracledb.DB_TYPE_VARCHAR, dir: oracledb.BIND_OUT },
  };

  if (_.includes(additionalFields, 'laborDistribution') && body.laborDistribution !== undefined) {
    formatLaborDistributionForDb(body, binds);
  }

  const flattenedBody = flattenBody(body);

  // Some boolean fields need to be massaged to be compatible with db
  if (_.includes(additionalFields, 'accruesLeaveInd')
      && flattenedBody.accruesLeaveInd !== undefined) {
    flattenedBody.accruesLeaveInd = flattenedBody.accruesLeaveInd ? 'Y' : 'N';
  }
  if (_.includes(additionalFields, 'useTemporarySsnInd')
      && flattenedBody.useTemporarySsnInd !== undefined) {
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

  _.merge(binds, _.pick(flattenedBody, [
    'positionNumber',
    'suffix',
    'effectiveDate',
    ...additionalFields || [],
  ]));

  return binds;
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
  const binds = standardBinds(osuId, body, [
    'hourlyRate',
    'appointmentPercent',
    'personnelChangeDate',
    'hoursPerPay',
    'annualSalary',
    'fullTimeEquivalency',
    'changeReason_code',
    'salary_annual',
  ]);

  const { outBinds: { result } } = await connection.execute(contrib.updateJob(binds), binds);
  return result;
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
  const binds = standardBinds(osuId, body, ['changeReason_code']);

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

  const { outBinds: { result } } = await connection.execute(contrib.updateLaborChangeJob(), binds);
  return result;
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

    if (employmentType === 'student' && !/^C5[0-2]/.test(body.positionNumber)) {
      return new Error('Position number for students must start with C50, C51, or C52');
    }

    if (_.includes(['TERME', 'TERMJ'], changeReasonCode)) {
      result = await terminateJob(connection, osuId, body);
    } else {
      if (!await isValidChangeReasonCode(connection, changeReasonCode)) {
        return new Error(`Invalid change reason code ${changeReasonCode}`);
      }

      if (update) {
        if (changeReasonCode === 'NONE') {
          result = await updateLaborChangeJob(connection, osuId, body);
        } else if (changeReasonCode === 'BREAP') {
          if (employmentType === 'student') {
            result = await studentUpdateJob(connection, osuId, body);
          } else if (employmentType === 'graduate') {
            result = await graduateUpdateJob(connection, osuId, body);
          }
        } else {
          await updateJob(connection, osuId, body);
        }
      } else if (employmentType === 'student') {
        result = await studentCreateJob(connection, osuId, body);
      } else if (employmentType === 'graduate') {
        result = await graduateCreateJob(connection, osuId, body);
      }
    }
    // null === success
    if (!result) {
      connection.commit();
      return result;
    }
    throw new Error(result);
  } finally {
    connection.close();
  }
};

export { getJobs, getJobByJobId, createOrUpdateJob };
