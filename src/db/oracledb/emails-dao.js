import _ from 'lodash';
import oracledb from 'oracledb';

import { parseQuery } from 'utils/parse-query';
import { getConnection } from './connection';
import { contrib } from './contrib/contrib';

/**
 * Query data source for email data
 *
 * @param {object} connection oracledb connection object
 * @param {string} internalId Internal ID of a person
 * @param {object} query Query parameters passed in with request
 * @returns {Promise<object[]>} raw email data
 */
const getEmailsByOsuIdWithConnection = async (connection, internalId, query) => {
  const parsedQuery = parseQuery(query);
  const { rows } = await connection.execute(
    contrib.getEmailsByOsuId(parsedQuery),
    { internalId, ..._.pick(parsedQuery, ['emailRowId']) },
  );
  return rows;
};

/**
 * Query data source for email data
 *
 * @param {string} internalId Internal ID of a person
 * @param {object} query Query parameters passed in with request
 * @returns {Promise<object[]>} raw email data
 */
const getEmailsByOsuId = async (internalId, query) => {
  const connection = await getConnection('banner');
  try {
    return await getEmailsByOsuIdWithConnection(connection, internalId, query);
  } finally {
    connection.close();
  }
};

/**
 * Checks for preferred emails for a given person
 *
 * @param {string} internalId
 * @returns {string} ID of preferred email address
 */
const preferredEmailExists = async (internalId) => {
  const connection = await getConnection('banner');
  try {
    const { rows } = await connection.execute(contrib.hasPreferredEmail(), { internalId });
    return rows ? rows[0].emailId : undefined;
  } finally {
    connection.close();
  }
};

/**
 * Creates an email record for a user
 *
 * @param {string} internalId Internal ID of a person
 * @param {object} body Body passed in with request
 * @returns {Promise<object>} Raw email data for the newly created email
 */
const createEmail = async (internalId, body) => {
  const connection = await getConnection('banner');
  try {
    const binds = {
      internalId,
      emailAddress: body.emailAddress,
      emailType: body.emailType.code,
      result: { type: oracledb.DB_TYPE_VARCHAR, dir: oracledb.BIND_OUT },
    };
    if (body.preferredInd !== undefined) {
      binds.preferredInd = body.preferredInd ? 'Y' : 'N';
    }
    // comment is reserved in oracledb
    if (body.comment !== undefined) {
      binds.emailComment = body.comment;
    }
    const { outBinds: { result } } = await connection.execute(contrib.createEmail(binds), binds);

    const rows = await getEmailsByOsuIdWithConnection(
      connection,
      internalId,
      { emailRowId: result },
    );

    return rows[0];
  } finally {
    await connection.rollback();
    connection.close();
  }
};

/**
 * Query for a single email address by email ID
 *
 * @param {string} internalId internal ID of a person
 * @param {string} emailId ID of an email address
 * @returns {Promise<object>} raw email data
 */
const getEmailByEmailId = async (internalId, emailId) => {
  const connection = await getConnection('banner');
  try {
    const binds = { internalId, emailId };
    const { rows } = await connection.execute(contrib.getEmailsByOsuId(binds), binds);

    if (rows.length > 1) {
      throw new Error('Multiple emails found when querying by ID');
    }

    return rows ? rows[0] : undefined;
  } finally {
    connection.close();
  }
};

export {
  getEmailsByOsuId,
  createEmail,
  preferredEmailExists,
  getEmailByEmailId,
};
