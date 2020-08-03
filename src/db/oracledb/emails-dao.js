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
 * Creates an email record for a user
 *
 * @param {string} internalId Internal ID of a person
 * @param {object} body Body passed in with request
 * @returns {Promise<object>} Raw email data for the newly created email
 */
const createEmail = async (internalId, body) => {
  const connection = await getConnection('banner');
  try {
    const { rows: existingEmails } = await connection.execute(
      contrib.hasSameEmailType(),
      { internalId, emailType: body.emailType.code },
    );

    if (!_.isEmpty(existingEmails)) {
      const binds = { ...existingEmails[0], internalId };
      await connection.execute(contrib.deactivateEmail(), binds);
    }

    const binds = _.omit(body, ['comment']);
    binds.internalId = internalId;
    binds.emailType = binds.emailType.code;
    binds.preferredInd = binds.preferredInd ? 'Y' : 'N';
    binds.result = { type: oracledb.DB_TYPE_VARCHAR, dir: oracledb.BIND_OUT };
    // comment is reserved in oracledb
    binds.emailComment = body.comment;
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

export { getEmailsByOsuId, createEmail };
