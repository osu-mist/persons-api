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
const getAddressesByOsuId = async (osuId, query) => {
  const connection = await getConnection();
  try {
    const parsedQuery = parseQuery(query);
    parsedQuery.osuId = osuId;

    const { rows } = await connection.execute(contrib.getAdresses(parsedQuery), parsedQuery);

    return serializeAddresses(rows, query, osuId);
  } finally {
    connection.close();
  }
};

export { getAddressesByOsuId };
