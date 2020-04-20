import oracledb from 'oracledb';

import { parseQuery } from 'utils/parse-query';
import { getConnection } from './connection';
import { contrib } from './contrib/contrib';

/**
 * Queries data source for raw address data and passes it to the serializer before returning
 *
 * @param {string} internalId internal ID of person to select addresses from
 * @param {object} query Query parameters passed in with request
 * @returns {Promise<object>} Serialized address resource
 */
const getAddressesByInternalId = async (internalId, query) => {
  const connection = await getConnection();
  try {
    const parsedQuery = parseQuery(query);
    parsedQuery.internalId = internalId;

    const { rows } = await connection.execute(contrib.getAdresses(parsedQuery), parsedQuery);
    return rows;
  } finally {
    connection.close();
  }
};

const hasSameAddressType = async (internalId, addressType) => {
  const connection = await getConnection();
  try {
    const attributes = { internalId, addressType };
    const { rows } = await connection.execute(contrib.hasSameAddressType(), attributes);

    return rows[0];
  } finally {
    connection.close();
  }
};

const phoneHasSameAddressType = async (internalId, addressType) => {
  const connection = await getConnection();
  try {
    const attributes = { internalId, addressType };
    const { rows } = await connection.execute(contrib.phoneHasSameAddressType(), attributes);

    return rows[0];
  } finally {
    connection.close();
  }
};

const updatePhoneAddrSeqno = async (internalId, address, phone) => {
  if (phone) {
    const updatedAddress = await hasSameAddressType(internalId, address['addressType.code']);

    const connection = await getConnection();
    try {
      phone.addrSeqno = updatedAddress.seqno;
      await connection.execute(contrib.updatePhoneAddrSeqno(), phone);
    } finally {
      connection.close();
    }
  }
};

/**
 * Creates address records
 */
const createAddress = async (internalId, body) => {
  const connection = await getConnection();
  try {
    body.addressType = body.addressType.code;
    body.seqno = null;
    body.pidm = internalId;
    body.returnValue = { type: oracledb.DB_TYPE_VARCHAR, dir: oracledb.BIND_OUT };

    const address = await hasSameAddressType(internalId, body.addressType);
    const phone = await phoneHasSameAddressType(internalId, body.addressType);
    if (address) {
      const deactivateBinds = { ...address, internalId };
      await connection.execute(contrib.deactivateAddress(), deactivateBinds);
    }

    await connection.execute(contrib.createAddress(body), body);
    const newAddress = await getAddressesByInternalId(
      internalId,
      { 'filter[addressType]': body.addressType },
    );
    if (newAddress.length > 1) {
      const reactivateBinds = { ...address, internalId };
      await connection.execute(contrib.reactivateAddress(), reactivateBinds);
      throw new Error('error when creating address');
    }

    await updatePhoneAddrSeqno(internalId, newAddress[0], phone);

    return newAddress[0];
  } finally {
    connection.close();
  }
};

export { getAddressesByInternalId, createAddress };
