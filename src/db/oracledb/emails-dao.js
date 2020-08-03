import _ from 'lodash';

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
      contrib.getEmailByInternalId(),
      { internalId, emailType: body.emailType.code },
    );

    if (!_.isEmpty(rows)) {
      const binds = { ...rows[0], internalId };
      await connection.execute(contrib.deactivateEmail(), binds);
    }

    return undefined;
  } finally {
    await connection.rollback();
    connection.close();
  }
};

export { getEmailsByOsuId, createEmail };
