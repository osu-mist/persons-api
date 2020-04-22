import _ from 'lodash';
import oracledb from 'oracledb';

import { parseQuery } from 'utils/parse-query';
import { hasSameAddressType } from 'db/oracledb/addresses-dao';
import { getConnection } from './connection';
import { contrib } from './contrib/contrib';

const getPhones = async (connection, internalId, query) => {
  const parsedQuery = parseQuery(query);
  parsedQuery.internalId = internalId;

  const omittedQuery = _.omit(parsedQuery, ['primaryInd']);
  const { rows } = await connection.execute(contrib.getPhones(parsedQuery), omittedQuery);

  return rows;
};

/**
 * Queries data source for raw person data and passes it to the serializer
 *
 * @param {string} internalId internal ID of person to select
 * @param {object} query query parameters passed in with the request
 * @returns {Promise<object>} Raw phone data from data source
 */
const getPhonesByInternalId = async (internalId, query) => {
  const connection = await getConnection();
  try {
    return await getPhones(connection, internalId, query);
  } finally {
    connection.close();
  }
};

const hasSamePhoneType = async (connection, internalId, phoneType) => {
  const binds = { internalId, phoneType };
  const { rows } = await connection.execute(contrib.hasSamePhoneType(), binds);

  return rows[0];
};

const deactivatePhone = async (connection, phone) => {
  const binds = _.pick(phone, ['internalId', 'seqno', 'phoneType', 'phoneId']);
  await connection.execute(contrib.deactivatePhone(), binds);
};

/**
 * Create phone record
 *
 * @param {string} internalId Internal ID of a person
 * @param {object} body body sent with post request
 * @returns {Promise<object>} newly created phone record
 */
const postPhones = async (internalId, body) => {
  const connection = await getConnection();
  try {
    body.internalId = internalId;
    body.addressType = body.addressType.code;
    body.phoneType = body.phoneType.code;
    body.phoneId = { type: oracledb.DB_TYPE_VARCHAR, dir: oracledb.BIND_OUT };
    body.seqno = { type: oracledb.DB_TYPE_VARCHAR, dir: oracledb.BIND_OUT };

    const address = await hasSameAddressType(connection, internalId, body.addressType);
    if (!address) {
      // should be using errorBuilder somehow
      throw new Error('Address record does not exists for this person and address type');
    }
    body.addrSeqno = address.seqno;

    const phone = await hasSamePhoneType(connection, internalId, body.phoneType);
    if (phone) {
      await deactivatePhone(connection, phone);
    }

    // remove primaryInd from body since it can't be passed in with connection.execute
    const binds = _.omit(body, ['primaryInd']);
    await connection.execute(contrib.createPhone(body), binds);

    const newPhones = await getPhones(
      connection,
      internalId,
      { 'filter[phoneType]': body.phoneType },
    );
    if (newPhones.length > 1) {
      throw new Error(`Error: Multiple active phones for phone type ${body.phoneType}`);
    } else if (newPhones.length === 0) {
      throw new Error('Error: No phone record created');
    }

    return newPhones[0];
  } finally {
    connection.close();
  }
};

export { getPhonesByInternalId, postPhones };
