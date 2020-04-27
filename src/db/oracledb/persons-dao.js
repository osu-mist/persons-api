import _ from 'lodash';
import { DB_TYPE_VARCHAR, BIND_OUT } from 'oracledb';

import { getConnection } from './connection';
import { contrib } from './contrib/contrib';

/**
 * Returns true if person with the given OSU ID exists
 *
 * @param {string} osuId OSU ID of a person
 * @returns {boolean} true if person exists
 */
const personExists = async (osuId) => {
  const connection = await getConnection('banner');
  try {
    const { rows } = await connection.execute(contrib.personExists(), { osuId });
    return rows.length > 0 ? rows[0].internalId : null;
  } finally {
    connection.close();
  }
};

/**
 * Uses passed in connection to query data source for person records
 *
 * @param {object} connection oracledb connection
 * @param {string} osuId OSU ID of a person
 */
const getPerson = async (connection, osuId) => {
  const query = { osuId };
  const { rows } = await connection.execute(contrib.getPersonById(), query);

  if (rows.length > 1) {
    throw new Error('Expect a single object but got multiple results.');
  }

  return !_.isEmpty(rows) ? rows[0] : undefined;
};

/**
 * Queries data source for raw person data and passes it to the serializer
 *
 * @param {string} osuId OSU ID of person to select
 * @returns {Promise<object>} Serialized person resource from person-serializer
 */
const getPersonById = async (osuId) => {
  const connection = await getConnection('banner');
  try {
    return await getPerson(connection, osuId);
  } finally {
    connection.close();
  }
};

/**
 * Creates person record
 *
 * @param {object} body body from request
 * @returns {Promise<object>} Newly created person record
 */
const createPerson = async (body) => {
  const connection = await getConnection('banner');
  try {
    if (body.citizen && body.citizen.code) {
      body.citizen = body.citizen.code;
    }
    body.outId = { type: DB_TYPE_VARCHAR, dir: BIND_OUT };

    const { outBinds: { outId } } = await connection.execute(contrib.createPerson(body), body);

    const person = await getPerson(connection, outId);
    console.log(person);
    if (!person) {
      connection.rollback();
      throw new Error('Person creation failed');
    }

    // await connection.commit();
    return person;
  } finally {
    connection.close();
  }
};

export { getPersonById, personExists, createPerson };
