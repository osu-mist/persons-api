/* eslint-disable no-unused-vars */
import _ from 'lodash';

import { getConnection } from './connection';
import { contrib } from './contrib/contrib';
import { serializePersons } from '../../serializers/persons-serializer';

/**
 * Queries data source for raw person data and passes it to the serializer
 *
 * @param {object} querys Query parameters from request
 * @returns {object} Serialized person resource from person-serializer
 */
const getPerson = async (querys) => {
  const connection = await getConnection();
  try {
    console.log(querys);
    const { rows } = await connection.execute(contrib.getPerson(querys.osuId), {
      osuUid: querys.osuUid,
      onid: querys.onid,
      firstName: querys.lastName ? querys.firstName.toUpperCase() : null,
      lastName: querys.lastName ? querys.lastName.toUpperCase() : null,
      searchOldVersions: querys.searchOldVersions || 0,
    });
    if (rows.length > 0) {
      const internalIds = _.map(rows, 'internalId');
      const { rows: previousRecords } = await connection.execute(
        contrib.getPreviousRecords(internalIds),
      );
      const previousRecordsMap = _.groupBy(previousRecords, 'osuId');
      _.forEach(rows, (rawPerson) => {
        rawPerson.previousRecords = previousRecordsMap[rawPerson.osuId] || [];
        rawPerson.currentEmployee = rawPerson.employeeStatus === 'A';
        rawPerson.currentStudent = rawPerson.studentStatus === 'Y';
      });
    }

    if (rows.length === 1) {
      return serializePersons(rows[0], querys);
    }
    return serializePersons(rows, querys);
  } finally {
    connection.close();
  }
};

export {
  getPerson,
};
