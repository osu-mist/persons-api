/* eslint-disable no-unused-vars */
import _ from 'lodash';

import { getConnection } from './connection';
import { contrib } from './contrib/contrib';
import { serializePerson } from '../../serializers/persons-serializer';

const getPerson = async (osuId) => {
  const connection = await getConnection();
  try {
    console.log('getPerson dao');
    const { rows: rawPerson } = await connection.execute(contrib.getPerson(), {
      osuUID: null,
      onid: null,
      firstName: null,
      lastName: null,
      searchOldVersions: null,
      osuIds: (osuId),
    });
    const { rows: previousRecords } = await connection.execute(contrib.getPreviousRecords(), {
      id: rawPerson[0].internalId,
    });
    console.log(rawPerson[0]);
    rawPerson[0].previousRecords = previousRecords;
    rawPerson[0].currentEmployee = rawPerson[0].employeeStatus === 'A';
    rawPerson[0].currentStudent = rawPerson[0].studentStatus === 'Y';

    return serializePerson(rawPerson[0]);
  } finally {
    connection.close();
  }
};

export {
  getPerson,
};
