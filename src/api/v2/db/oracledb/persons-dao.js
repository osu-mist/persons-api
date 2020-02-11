/* eslint-disable no-unused-vars */
import _ from 'lodash';

import { getConnection } from './connection';
import { contrib } from './contrib/contrib';

const getPerson = async (osuId) => {
  const connection = await getConnection();
  try {
    console.log('getPerson');
    const { rows } = await connection.execute(contrib.getPerson(), {
      osuUID: null,
      onid: null,
      firstName: null,
      lastName: null,
      searchOldVersions: null,
      osuIds: (osuId),
    });
    console.log(rows);
    return rows;
  } finally {
    connection.close();
  }
};

export {
  getPerson,
};
