/* eslint-disable no-unused-vars */
import _ from 'lodash';

import { getConnection } from './connection';
import { contrib } from './contrib/contrib';

const getPerson = async (osuId) => {
  const connection = await getConnection();
  try {
    console.log('getPerson');
    const { rows } = await connection.execute(contrib.getPerson(), {
      osuUID: '932776660',
      onid: 'ruefa',
      firstName: 'alexander',
      lastName: 'ruef',
      searchOldVersions: '0',
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
