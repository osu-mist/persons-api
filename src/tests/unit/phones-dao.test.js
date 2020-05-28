import chai from 'chai';
import chaiAsPromised from 'chai-as-promised';
import _ from 'lodash';
import proxyquire from 'proxyquire';
import sinon from 'sinon';

import { logger } from 'utils/logger';
import { fakeOsuId, fakePhoneBody } from './mock-data';
import { createDaoProxy } from './test-helpers';

chai.should();
chai.use(chaiAsPromised);

describe('Test phones-dao', () => {
  const daoPath = '../../db/oracledb/phones-dao';

  sinon.stub(logger, 'error').returns(null);

  const testCases = [
    {
      message: 'getPhonesByInternalId should return multiple results',
      functionName: 'getPhonesByInternalId',
      dbReturn: { rows: [{}, {}] },
      expected: [{}, {}],
    },
    {
      message: 'createPhone should return single result',
      functionName: 'createPhone',
      dbReturn: { rows: [{}] },
      expected: {},
    },
  ];
  _.forEach(testCases, ({
    message,
    functionName,
    dbReturn,
    expected,
  }) => {
    it(message, () => {
      const daoProxy = createDaoProxy(daoPath, dbReturn);
      const result = daoProxy[functionName](fakeOsuId, _.clone(fakePhoneBody));
      return result.should.eventually.be.fulfilled.and.deep.equal(expected);
    });
  });

  it('createPhone should return error when an address record does not exist', () => {
    const daoProxy = createDaoProxy(daoPath, { rows: [] });
    const result = daoProxy.createPhone(fakeOsuId, _.clone(fakePhoneBody));
    return result.should.eventually.be.a('error');
  });

  const errorCases = [
    {
      message: 'newPhones returned has more than 1 record',
      dbReturn: { rows: [{}, {}] },
      errorMessage: 'Error: Multiple active phones for phone type undefined',
    },
    {
      message: 'newPhones returned no records',
      dbReturn: { rows: [] },
      errorMessage: 'Error: No phone record created',
    },
  ];
  _.forEach(errorCases, ({ message, dbReturn, errorMessage }) => {
    it(`createPhone should throw error when ${message}`, () => {
      // need unique dao proxy for this
      const executeStub = sinon.stub().returns({ outBinds: { seqno: {} }, rows: [{}] });
      executeStub.onCall(4).returns(dbReturn);
      const daoProxy = proxyquire(daoPath, {
        './connection': {
          getConnection: sinon.stub().resolves({
            execute: executeStub,
            close: () => null,
            commit: () => null,
            rollback: () => null,
          }),
        },
      });
      const result = daoProxy.createPhone(fakeOsuId, _.clone(fakePhoneBody));
      return result.should.eventually
        .be.rejectedWith(errorMessage);
    });
  });

  sinon.restore();
});
