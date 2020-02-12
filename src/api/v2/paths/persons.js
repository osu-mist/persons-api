/* eslint-disable no-unused-vars */
import { errorHandler } from 'errors/errors';
import { getPerson } from '../db/oracledb/persons-dao';

const get = async (req, res) => {
  console.log('get person endpoint');
  try {
    const { osuId } = req.query;
    const result = await getPerson(osuId[0]);
    return res.send(result);
  } catch (err) {
    return errorHandler(res, err);
  }
};

const post = async (req, res) => {
  // todo
};

export {
  get,
  post,
};
