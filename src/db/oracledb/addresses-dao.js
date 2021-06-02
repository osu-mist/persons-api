import { parseQuery } from 'utils/parse-query';
import { getConnection } from './connection';
import { contrib } from './contrib/contrib';

/**
 * Queries data source for address data using passed in oracledb connection
 *
 * @param {object} connection open oracledb connection
 * @param {string} internalId internal ID of a person
 * @param {object} query query parameters passed in with request
 * @returns {Promise<object[]>} raw address data from data source
 */
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
  const connection = await getConnection('banner');
  try {
    return await getAddresses(connection, internalId, query);
  } finally {
    connection.close();
  }
};

/**
 * Check if an address exists for the given address type
 *
 * @param {object} connection oracledb connection object
 * @param {string} internalId internal ID of a person
 * @param {string} addressType Address type code
 * @returns {Promise<object>} Limited address record fields
 */
const hasSameAddressType = async (connection, internalId, addressType) => {
  const attributes = { internalId, addressType };
  const { rows } = await connection.execute(contrib.hasSameAddressType(), attributes);

  if (rows.length > 1) {
    throw new Error(
      `Multiple addresses found for the same address type ${addressType} for ${internalId}`,
    );
  }

  return rows[0];
};

/**
 * Query for phones by address type
 *
 * @param {object} connection oracle db connection object
 * @param {string} internalId Internal ID of a person
 * @param {string} addressType Address type from request
 * @returns {object} raw phone data
 */
const phoneHasSameAddressType = async (connection, internalId, addressType) => {
  const attributes = { internalId, addressType };
  const { rows } = await connection.execute(contrib.phoneHasSameAddressType(), attributes);

  if (rows.length > 1) {
    throw new Error(
      `Multiple phone records found for the address type ${addressType} for ${internalId}`,
    );
  }

  return rows[0];
};

export {
  getAddressesByInternalId,
  hasSameAddressType,
  phoneHasSameAddressType,
};
