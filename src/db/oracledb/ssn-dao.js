import { DB_TYPE_VARCHAR, BIND_OUT } from 'oracledb';

import { getConnection } from './connection';
import { contrib } from './contrib/contrib';

const ssnIsNotNull = async (osuId) => {
  const connection = await getConnection('banner');
  try {
    const { rows: { ssnStatus } } = await connection.execute(contrib.ssnIsNotNull(), { osuId });

    return ssnStatus === 'Y';
  } finally {
    connection.close();
  }
};

const createSsn = async (internalId, body) => {
  const connection = await getConnection('banner');
  try {
    body.internalId = internalId;
    body.returnValue = { type: DB_TYPE_VARCHAR, dir: BIND_OUT };

    const { outbinds } = await connection.execute(contrib.createSsn(), body);
    console.log(outbinds);

    return undefined;
  } finally {
    connection.close();
  }
};

export { createSsn, ssnIsNotNull };
