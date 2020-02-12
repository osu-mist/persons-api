/* eslint-disable no-unused-vars */
import { errorHandler } from 'errors/errors';
import { getPerson } from '../db/oracledb/persons-dao';

const get = async (req, res) => {
  console.log('get person endpoint');
  const { osuId } = req.query;
  const serializedPerson = await getPerson(osuId[0]);
  return res.send(serializedPerson);
};

const post = async (req, res) => {
  // todo
};

export {
  get,
  post,
};
