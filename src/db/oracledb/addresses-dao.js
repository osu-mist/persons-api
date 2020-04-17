import _ from 'lodash';
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
const getAddressesByOsuId = async (internalId, query) => {
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

    const addresses = await getAddressesByOsuId(internalId, { addressType: 'EO' });
    if (addresses.length > 0) {
      console.log('address exists, deactivating');
      console.log(addresses);
      const deactivateBinds = _.pick(body, ['pidm', 'addressType']);
      const output = await connection.execute(contrib.deactivateAddress(), deactivateBinds);
      console.log(output);
    }

    const { outBinds } = await connection.execute(contrib.createAddress(body), body);
    console.log(outBinds);
    return outBinds;
  } finally {
    connection.close();
  }
};

export { getAddressesByOsuId, createAddress };
