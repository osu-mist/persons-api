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

    return rows;
  } finally {
    connection.close();
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

    const addresses = await hasSameAddressType(internalId, 'EO');
    if (addresses.length > 1) {
      // error state?
    } else if (addresses.length === 1) {
      console.log('address exists, deactivating');
      const deactivateBinds = { ...addresses[0], internalId };
      console.log(deactivateBinds);
      const output = await connection.execute(contrib.deactivateAddress(), deactivateBinds);
      console.log(output);
    }

    console.log('creating address');
    const { outBinds } = await connection.execute(contrib.createAddress(body), body);
    console.log(outBinds);
    return outBinds;
  } finally {
    connection.close();
  }
};

export { getAddressesByInternalId, createAddress };
