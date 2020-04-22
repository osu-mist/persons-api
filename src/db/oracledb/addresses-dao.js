import oracledb from 'oracledb';

import { parseQuery } from 'utils/parse-query';
import { getConnection } from './connection';
import { contrib } from './contrib/contrib';

const getAddresses = async (connection, internalId, query) => {
  const parsedQuery = parseQuery(query);
  parsedQuery.internalId = internalId;

  const { rows } = await connection.execute(contrib.getAdresses(parsedQuery), parsedQuery);
  return rows;
};

/**
 * Queries data source for raw address data and passes it to the serializer before returning
 *
 * @param {string} internalId internal ID of person to select addresses from
 * @param {object} query Query parameters passed in with request
 * @returns {Promise<object>} Raw address records from data source
 */
const getAddressesByInternalId = async (internalId, query) => {
  const connection = await getConnection();
  try {
    return await getAddresses(connection, internalId, query);
  } finally {
    connection.close();
  }
};

const hasSameAddressType = async (connection, internalId, addressType) => {
  const attributes = { internalId, addressType };
  const { rows } = await connection.execute(contrib.hasSameAddressType(), attributes);

  return rows[0];
};

const phoneHasSameAddressType = async (connection, internalId, addressType) => {
  const attributes = { internalId, addressType };
  const { rows } = await connection.execute(contrib.phoneHasSameAddressType(), attributes);

  return rows[0];
};

const updatePhoneAddrSeqno = async (connection, addrSeqno, phone) => {
  if (phone) {
    phone.addrSeqno = addrSeqno;
    await connection.execute(contrib.updatePhoneAddrSeqno(), phone);
  }
};

/**
 * Creates address records
 *
 * @param {string} internalId internal ID of a person
 * @param {object} body Request body with new address attributes
 * @returns {Promise<object>} Raw address record from data source
 */
const createAddress = async (internalId, body) => {
  const connection = await getConnection();
  try {
    body.addressType = body.addressType.code;
    body.internalId = internalId;
    body.returnValue = { type: oracledb.DB_TYPE_VARCHAR, dir: oracledb.BIND_OUT };
    body.seqno = { type: oracledb.DB_TYPE_VARCHAR, dir: oracledb.BIND_OUT };

    // Query phone early because it is changed automatically by createAddress
    const phone = await phoneHasSameAddressType(connection, internalId, body.addressType);

    const address = await hasSameAddressType(connection, internalId, body.addressType);
    if (address) {
      const deactivateBinds = { ...address, internalId };
      await connection.execute(contrib.deactivateAddress(), deactivateBinds);
    }

    const result = await connection.execute(contrib.createAddress(body), body);

    const newAddress = await getAddresses(
      connection,
      internalId,
      { 'filter[addressType]': body.addressType },
    );
    if (newAddress.length > 1) {
      throw new Error(`Error: Multiple active addresses for address type ${body.addressType}`);
    }

    await updatePhoneAddrSeqno(connection, result.outBinds.seqno, phone);

    await connection.commit();
    return newAddress[0];
  } catch (err) {
    console.log(err);
    await connection.rollback();
    return undefined;
  } finally {
    connection.close();
  }
};

export { getAddressesByInternalId, createAddress };
