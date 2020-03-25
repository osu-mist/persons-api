import { parseQuery } from 'utils/parse-query';
import { getConnection } from './connection';
import { contrib } from './contrib/contrib';

const getEmailsByOsuId = async (osuId, query) => {
  const connection = await getConnection();
  try {
    console.log('emails dao');
    const parsedQuery = parseQuery(query);
    const { rows } = await connection.execute(contrib.getEmailsByOsuId(parsedQuery), { osuId });
    return rows;
  } finally {
    connection.close();
  }
};

export { getEmailsByOsuId };
