import { serializePhones } from 'serializers/phones-serializer';
import { parseQuery } from 'utils/parse-query';
import { getConnection } from './connection';
import { contrib } from './contrib/contrib';

/**
 * Queries data source for raw person data and passes it to the serializer
 *
 * @param {string} osuId OSU ID of person to select
 */
const getPhones = async (osuId, query) => {
  const connection = await getConnection();
  try {
    console.log('getPhones dao');
    const parsedQuery = parseQuery(query);
    parsedQuery.osuId = osuId;
    parsedQuery.phoneType = parsedQuery.phoneType ? parsedQuery.phoneType : null;
    parsedQuery.addressType = parsedQuery.addressType ? parsedQuery.addressType : null;
    const { rows } = await connection.execute(contrib.getPhones(), parsedQuery);

    return serializePhones(rows, osuId, query);
  } finally {
    connection.close();
  }
};

export { getPhones };
