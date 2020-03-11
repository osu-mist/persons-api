/* eslint-disable no-unused-vars */
import _ from 'lodash';

import { getConnection } from './connection';
import { contrib } from './contrib/contrib';
import { serializePerson } from '../../serializers/persons-serializer';

/**
 * Queries data source for raw person data and passes it to the serializer
 *
 * @param {object} querys Query parameters from request
 * @returns {object} Serialized person resource from person-serializer
 */
const getPersonById = async (osuId) => {
  const connection = await getConnection();
  try {
    const { rows } = await connection.execute(contrib.getPersonById(osuId));

    const serializedPerson = serializePerson(rows[0], null);
    return serializedPerson;
    /*
      _.forEach(rows, (rawPerson) => {
        rawPerson.previousRecords = previousRecordsMap[rawPerson.osuId] || [];
        rawPerson.currentEmployee = rawPerson.employeeStatus === 'A';
        rawPerson.currentStudent = rawPerson.studentStatus === 'Y';
      });
    }

    if (rows.length === 1) {
      return serializePersons(rows[0], querys);
    }
    return serializePersons(rows, querys); */
  } finally {
    connection.close();
  }
};

export {
  getPersonById,
};
