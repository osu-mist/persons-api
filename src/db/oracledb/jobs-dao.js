import _ from 'async-dash';
import { flatten } from 'flat';
import moment from 'moment';
import oracledb from 'oracledb';

import { parseQuery } from 'utils/parse-query';
import { getConnection } from './connection';
import { contrib } from './contrib/contrib';

const commonBinds = [
  'changeReason_code',
  'employeeClassification_code',
  'hourlyRate',
  'personnelChangeDate',
  'appointmentPercent',
  'hoursPerPay',
  'salary_annual',
  'fullTimeEquivalency',
  'supervisor_positionNumber',
  'supervisor_suffix',
  'supervisor_osuId',
  'employeeGroup_code',
];

const studentBinds = [
  ...commonBinds,
  'status_code',
  'timesheet_current_code',
  'jobDescription',
  'campus_code',
  'salary_paysPerYear',
  'strsAppointmentBasis',
  'laborDistribution',
  'earningCode_effectiveDate',
  'earningCode_code',
  'earningCode_hours',
  'beginDate',
  'accruesLeaveInd',
  'effectiveDate',
  'contractBeginDate',
  'contractEndDate',
  'useTemporarySsnInd',
  'employeeInformationReleaseInd',
  'i9Form_code',
  'i9Form_date',
  'i9Form_expirationDate',
  'retirement_code',
  'homeOrganization_current_code',
];

const graduateBinds = [
  ...commonBinds,
  'status_code',
  'homeOrganization_current_code',
];

const validStudentPositionNumberPrefixes = ['C50', 'C51', 'C52'];
const validGradTermPositionNumberPrefixes = ['C50', 'C51', 'C52', 'C60', 'C69'];

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
    const binds = _.omit(parsedQuery, ['studentEmployeeInd']);

    const { rows } = await connection.execute(contrib.getJobs(parsedQuery), binds);

    await getLaborDistributions(connection, internalId, rows);

    return rows;
  } finally {
    connection.close();
  }
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
    const [positionNumber, suffix] = jobId.split('-');
    if (!positionNumber || !suffix) {
      return undefined;
    }

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

/**
 * Flattens body object into a 1 layer deep object using _ as a key delimiter
 *
 * @param {object} body Request body
 * @returns {object} flattened body
 */
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
  if (
    _.includes(additionalFields, 'accruesLeaveInd')
    && flattenedBody.accruesLeaveInd !== undefined
  ) {
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
 * Terminates a job record
 *
 * @param {object} connection oracledb connection
 * @param {string} osuId OSU ID of a person
 * @param {object} body Request body
 * @returns {string} Query result, null if success
 */
const terminateJob = async (connection, osuId, body) => {
  const binds = standardBinds(osuId, body, ['changeReason_code', 'personnelChangeDate']);

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
 * Creates student job records
 *
 * @param {object} connection oracledb connection
 * @param {string} osuId OSU ID of a person
 * @param {object} body Request body
 * @returns {string} Query result, null if success
 */
const studentJob = async (connection, osuId, body) => {
  const binds = standardBinds(osuId, body, studentBinds);

  const { outBinds: { result } } = await connection.execute(
    contrib.studentJob(binds),
    binds,
  );
  return result;
};

/**
 * Update or create graduate job record
 *
 * @param {object} connection oracledb connection
 * @param {string} osuId OSU ID of a person
 * @param {object} body Request body
 * @returns {string} Query result, null if success
 */
const graduateJob = async (connection, osuId, body) => {
  const binds = standardBinds(osuId, body, graduateBinds);

  const { outBinds: { result } } = await connection.execute(
    contrib.graduateJob(binds),
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
 * @param {string} osuId OSU ID of a person
 * @param {object} body Request body
 * @returns {Error} returns error or null if no error occurred
 */
const handleJob = async (osuId, body) => {
  const connection = await getConnection('banner');
  try {
    let error;
    const { studentEmployeeInd, positionNumber, changeReason: { code: changeReasonCode } } = body;
    const employmentType = studentEmployeeInd ? 'student' : 'graduate';

    if (employmentType === 'student') {
      const termination = ['TERME', 'TERMJ'].includes(changeReasonCode);
      const posNumPrefix = positionNumber.substring(0, 3);
      if (termination
          && !validGradTermPositionNumberPrefixes.includes(posNumPrefix)) {
        return new Error('Valid position numbers for termination must begin with one of these '
          + `prefixes: ${validGradTermPositionNumberPrefixes.join(', ')}`);
      }
      if (!termination
          && !validStudentPositionNumberPrefixes.includes(posNumPrefix)) {
        return new Error('Student position numbers must begin with one of these prefixes: '
          + `${validStudentPositionNumberPrefixes.join(', ')}`);
      }
    }

    if (_.includes(['TERME', 'TERMJ'], changeReasonCode)) {
      error = await terminateJob(connection, osuId, body);
    } else {
      if (!await isValidChangeReasonCode(connection, changeReasonCode)) {
        return new Error(`Invalid change reason code ${changeReasonCode}`);
      }

      if (changeReasonCode === 'NONE') {
        error = await updateLaborChangeJob(connection, osuId, body);
      } else if (employmentType === 'student') {
        error = await studentJob(connection, osuId, body);
      } else if (employmentType === 'graduate') {
        error = await graduateJob(connection, osuId, body);
      }
    }

    if (!error) {
      await connection.commit();
      return error;
    }

    if (error.includes('JTRM')) {
      return new Error(error);
    }

    throw new Error(error);
  } finally {
    connection.close();
  }
};

export { getJobs, getJobByJobId, handleJob };
