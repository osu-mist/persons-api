import _ from 'lodash';
import oracledb from 'oracledb';

import { parseQuery } from 'utils/parse-query';
import { getConnection } from './connection';
import { contrib } from './contrib/contrib';

/**
 * Query data source for email data
 *
 * @param {string} internalId Internal ID of a person
 * @param {*} query Query parameters passed in with request
 * @returns {object[]} raw email data
 */
const getEmailsByOsuId = async (internalId, query) => {
  const connection = await getConnection('banner');
  try {
    const parsedQuery = parseQuery(query);
    const { rows } = await connection.execute(
      contrib.getEmailsByOsuId(parsedQuery),
      { internalId },
    );
    return rows;
  } finally {
    connection.close();
  }
};

const createEmail = async (internalId, body) => {
  const connection = await getConnection('banner');
  try {
    const { rows } = await connection.execute(
      contrib.hasSameEmailType(),
      { internalId, emailType: body.emailType.code },
    );

    if (!_.isEmpty(rows)) {
      const binds = { ...rows[0], internalId };
      await connection.execute(contrib.deactivateEmail(), binds);
    }

    // const binds = { ...body, internalId };
    const binds = _.omit(body, ['comment']);
    binds.internalId = internalId;
    binds.emailType = binds.emailType.code;
    binds.preferredInd = binds.preferredInd ? 'Y' : 'N';
    binds.result = { type: oracledb.DB_TYPE_VARCHAR, dir: oracledb.BIND_OUT };
    // comment is reserved in oracledb
    binds.emailComment = body.comment;
    console.log(binds);
    const { outBinds: { result } } = await connection.execute(contrib.createEmail(binds), binds);
    console.log(result);

    return undefined;
  } finally {
    await connection.rollback();
    connection.close();
  }
};

export { getEmailsByOsuId, createEmail };
