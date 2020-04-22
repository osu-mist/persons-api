import _ from 'lodash';
import oracledb from 'oracledb';

import { parseQuery } from 'utils/parse-query';
import { hasSameAddressType } from 'db/oracledb/addresses-dao';
import { getConnection } from './connection';
import { contrib } from './contrib/contrib';

/**
 * Queries data source for raw person data and passes it to the serializer
 *
 * @param {string} internalId internal ID of person to select
 * @param {object} query query parameters passed in with the request
 * @returns {Promise<object>} Raw phone data from data source
 */
const getPhones = async (internalId, query) => {
  const connection = await getConnection();
  try {
    const parsedQuery = parseQuery(query);
    parsedQuery.internalId = internalId;

    const omittedQuery = _.omit(parsedQuery, ['primaryInd']);
    const { rows } = await connection.execute(contrib.getPhones(parsedQuery), omittedQuery);

    return rows;
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

const postPhones = async (internalId, body) => {
  const connection = await getConnection();
  try {
    body.internalId = internalId;
    body.addressType = body.addressType.code;
    body.phoneType = body.phoneType.code;
    body.phoneId = { type: oracledb.DB_TYPE_VARCHAR, dir: oracledb.BIND_OUT };
    body.seqno = { type: oracledb.DB_TYPE_VARCHAR, dir: oracledb.BIND_OUT };
    const address = await hasSameAddressType(connection, internalId, body.addressType);
    const phone = await hasSamePhoneType(connection, internalId, body.phoneType);

    if (!address) {
      // should be using errorBuilder somehow
      throw new Error('Address record does not exists for this person and address type');
    }
    body.addrSeqno = address.seqno;

    if (phone) {
      console.log('phone record exists. Deactivating current one');
      await deactivatePhone(connection, phone);
    }

    // remove primaryInd from body since it can't be passed in with connection.execute
    const binds = _.omit(body, ['primaryInd']);
    const result = await connection.execute(contrib.createPhone(body), binds);
    console.log(result);

    return undefined;
  } finally {
    connection.close();
  }
};

export { getPhones, postPhones };
