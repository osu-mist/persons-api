/* eslint-disable no-unused-vars */
import { errorHandler } from 'errors/errors';
import { getPerson } from '../db/oracledb/persons-dao';
import { serializePerson } from '../serializers/persons-serializer';

const get = async (req, res) => {
  console.log('get person endpoint');
  const rawPerson = await getPerson(5);
  const serializedPerson = serializePerson(rawPerson);
  return res.send(serializedPerson);
};

const post = async (req, res) => {
  // todo
};

export {
  get,
  post,
};
