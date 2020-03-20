/* eslint-disable no-unused-vars */
import { getConnection } from './connection';
import { contrib } from './contrib/contrib';

/**
 * Queries data source for raw address data and passes it to the serializer before returning
 *
 * @param {string} osuId OSU ID of person to select addresses from
 * @param {object} query Query parameters passed in with request
 * @returns {Promise<object>} Serialized address resource
 */
const getAddressesById = async (osuId, query) => {
  console.log('get addresses dao');
  const connection = await getConnection();
  try {
    return undefined;
  } finally {
    connection.close();
  }
};

export { getAddressesById };
