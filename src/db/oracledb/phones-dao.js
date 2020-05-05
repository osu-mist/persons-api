import _ from 'lodash';
import { DB_TYPE_VARCHAR, BIND_OUT } from 'oracledb';

import { hasSameAddressType, phoneHasSameAddressType } from 'db/oracledb/addresses-dao';
import { parseQuery } from 'utils/parse-query';

import { getConnection } from './connection';
import { contrib } from './contrib/contrib';

/**
 * Queries phone data using passed in oracledb connection
 *
 * @param {object} connection
 * @param {string} internalId
 * @param {object} query
 * @returns {Promise<object[]>} raw phone data
 */
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
  const connection = await getConnection('banner');
  try {
    return await getPhones(connection, internalId, query);
  } finally {
    connection.close();
  }
};

const hasSamePhoneType = async (connection, internalId, phoneType) => {
  const binds = { internalId, phoneType };
  const { rows } = await connection.execute(contrib.hasSamePhoneType(), binds);

  if (rows.length > 1) {
    throw new Error(`Multiple records found for phone type ${phoneType} for ${internalId}`);
  }

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
const createPhone = async (internalId, body) => {
  const connection = await getConnection('banner');
  try {
    body.internalId = internalId;
    body.addressType = body.addressType.code;
    body.phoneType = body.phoneType.code;
    body.phoneId = { type: DB_TYPE_VARCHAR, dir: BIND_OUT };
    body.seqno = { type: DB_TYPE_VARCHAR, dir: BIND_OUT };

    const address = await hasSameAddressType(connection, internalId, body.addressType);
    if (!address) {
      return new Error('Address record does not exists for this person and address type');
    }
    body.addrSeqno = address.seqno;

    if (body.phoneType !== body.addressType) {
      const phoneAddressType = await phoneHasSameAddressType(
        connection,
        internalId,
        body.addressType,
      );
      if (phoneAddressType.primaryInd) {
        return new Error(
          'A primary phone record with the $addressType address code already exists',
        );
      }
    }

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

    await connection.commit();
    return newPhones[0];
  } finally {
    connection.close();
  }
};

export { getPhonesByInternalId, createPhone };
