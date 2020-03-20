/* eslint-disable no-unused-vars */
import { serializeAddresses } from 'serializers/addresses-serializer';
import { parseQuery } from 'utils/parse-query';
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
    const parsedQuery = parseQuery(query);
    parsedQuery.osuId = osuId;
    parsedQuery.addressType = parsedQuery.addressType ? parsedQuery.addressType : null;
    parsedQuery.city = parsedQuery.city ? parsedQuery.city : null;
    parsedQuery.county = parsedQuery.county ? parsedQuery.county : null;
    parsedQuery.stateCode = parsedQuery.stateCode ? parsedQuery.stateCode : null;
    parsedQuery.nationCode = parsedQuery.nationCode ? parsedQuery.nationCode : null;

    const { rows } = await connection.execute(contrib.getAdresses(), parsedQuery);

    return serializeAddresses(rows, query, osuId);
  } finally {
    connection.close();
  }
};

export { getAddressesById };
