/* eslint-disable no-unused-vars */
import _ from 'lodash';

// import { serializePerson } from 'serializers/persons-serializer';
import { parseQuery } from 'utils/parse-query';
import { getConnection } from './connection';
import { contrib } from './contrib/contrib';

/**
 * Queries data source for raw person data and passes it to the serializer
 *
 * @param {object} query Query parameters from request
 * @returns {Promise<object>} Serialized person resource from person-serializer
 */
const getJobs = async (query, osuId) => {
  const connection = await getConnection();
  try {
    console.log('getJobs dao');
    const parsedQuery = parseQuery(query);
    parsedQuery.osuId = osuId;
    console.log(parsedQuery);
    const { rows } = await connection.execute(contrib.getJobsById(parsedQuery), parsedQuery);
    console.log(rows);

    /* if (rows.length > 1) {
      throw new Error('Expect a single object but got multiple results.');
    } else if (_.isEmpty(rows)) {
      return undefined;
    }

    return serializePerson(rows[0]); */
    return undefined;
  } finally {
    connection.close();
  }
};

export { getJobs };
