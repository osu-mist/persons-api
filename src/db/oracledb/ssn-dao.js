import { DB_TYPE_VARCHAR, BIND_OUT } from 'oracledb';

import { getConnection } from './connection';
import { contrib } from './contrib/contrib';

/**
 * Check if a persons SSN is not null
 *
 * @param {string} internalId Internal ID of a person
 * @returns {boolean} True if person's SSN is not null
 */
const ssnIsNotNull = async (internalId) => {
  const connection = await getConnection('banner');
  try {
    const { rows } = await connection.execute(contrib.ssnIsNotNull(), { internalId });

    return rows.length > 0 && rows[0].ssnStatus === 'Y';
  } finally {
    connection.close();
  }
};

const hasSpbpers = async (connection, internalId) => {
  const { rows } = await connection.execute(contrib.hasSpbpers(), { internalId });

  return rows[0].spbpersCount >= 1;
};

/**
 * Creates an SSN record of updates it if one exists
 *
 * @param {string} internalId Internal ID of a person
 * @param {object} body body sent with request
 */
const createSsn = async (internalId, body) => {
  const connection = await getConnection('banner');
  try {
    body.internalId = internalId;

    if (await hasSpbpers(connection, internalId)) {
      await connection.execute(contrib.updateSsn(), body);
    } else {
      body.returnValue = { type: DB_TYPE_VARCHAR, dir: BIND_OUT };
      await connection.execute(contrib.createSsn(), body);
    }

    const { rows } = await connection.execute(contrib.ssnStatus(), { internalId });

    if (rows.length > 1 || rows[0].ssnStatus !== 'valid') {
      throw new Error('Error occurred creating SSN');
    }

    await connection.commit();
  } finally {
    connection.close();
  }
};

export { createSsn, ssnIsNotNull };
