import oracledb from 'oracledb';
import _ from 'lodash';

import { serializePerson } from 'serializers/persons-serializer';
import { getConnection } from './connection';
import { contrib } from './contrib/contrib';

const personExistsWithConnection = async (connection, osuId) => {
  const { rows } = await connection.execute(contrib.personExists(), { osuId });

  return rows.length > 0 ? rows[0].internalId : null;
};

/**
 * Returns true if person with the given OSU ID exists
 *
 * @param {string} osuId OSU ID of a person
 * @returns {boolean} true if person exists
 */
const personExists = async (osuId) => {
  const connection = await getConnection('banner');
  try {
    personExistsWithConnection(connection, osuId);
  } finally {
    connection.close();
  }
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
    const query = { osuId };
    const { rows } = await connection.execute(contrib.getPersonById(), query);

    if (rows.length > 1) {
      throw new Error('Expect a single object but got multiple results.');
    } else if (_.isEmpty(rows)) {
      return undefined;
    }

    return serializePerson(rows[0]);
  } finally {
    connection.close();
  }
};

const createPerson = async (body) => {
  const connection = await getConnection('banner');
  try {
    body.citizen = body.citizen.code;
    body.outId = { type: oracledb.DB_TYPE_VARCHAR, dir: oracledb.BIND_OUT };
    console.log(body);
    const { outBinds: { outId } } = await connection.execute(contrib.createPerson(), body);
    console.log(outId);
    if (!await personExistsWithConnection(connection, outId)) {
      connection.rollback();
      throw new Error('Person creation failed');
    }

    const person = await getPersonById(outId);

    // await connection.commit();
    return person;
  } finally {
    connection.close();
  }
};

export { getPersonById, personExists, createPerson };
