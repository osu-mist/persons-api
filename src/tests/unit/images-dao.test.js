import chai from 'chai';
import chaiAsPromised from 'chai-as-promised';
import _ from 'lodash';
import proxyquire from 'proxyquire';
import sinon from 'sinon';

import { logger } from 'utils/logger';
import { fakeOsuId } from './mock-data';

chai.should();
chai.use(chaiAsPromised);

describe('Test images-dao', () => {
  sinon.stub(logger, 'error').returns(null);

  const createDaoProxy = (dbReturn) => proxyquire('db/oracledb/images-dao', {
    './connection': {
      getConnection: sinon.stub().resolves({
        execute: () => dbReturn,
        close: () => null,
        commit: () => null,
        rollback: () => null,
      }),
    },
  });

  const testCases = [
    {
      message: 'getImageById should return image data on success',
      dbReturn: { rows: [{ image: 'image data' }] },
      expected: 'image data',
    },
    {
      message: 'getImageById should return undefined when rows is undefined',
      dbReturn: {},
      expected: undefined,
    },
    {
      message: 'getImageById should return undefined when rows is empty',
      dbReturn: { rows: [] },
      expected: undefined,
    },
  ];
  _.forEach(testCases, ({ message, dbReturn, expected }) => {
    it(message, () => {
      const daoProxy = createDaoProxy(dbReturn);
      const result = daoProxy.getImageById(fakeOsuId);
      return result.should.eventually.be.fulfilled.and.deep.equal(expected);
    });
  });

  sinon.restore();
});
