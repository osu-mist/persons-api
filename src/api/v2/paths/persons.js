/* eslint-disable no-unused-vars */
import { errorHandler } from 'errors/errors';
import { getPerson } from '../db/oracledb/persons-dao';
import { serializePerson } from '../serializers/persons-serializer';

const get = async (req, res) => {
  console.log('get person endpoint');
  const { osuID } = req.query;
  const rawPerson = await getPerson(osuID[0]);
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
