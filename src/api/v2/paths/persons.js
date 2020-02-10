/* eslint-disable no-unused-vars */
import { errorHandler } from 'errors/errors';

import { getConnection } from '../db/oracledb/connection';

const get = async (req, res) => {
  const connection = await getConnection();
  try {
    console.log('get person endpoint');
    return 'garbage';
  } catch (err) {
    return errorHandler(res, err);
  }
};

const post = async (req, res) => {
  try {
    console.log('post person endpoint');
    return 'test';
  } catch (err) {
    return errorHandler(res, err);
  }
};

export {
  get,
  post,
};
