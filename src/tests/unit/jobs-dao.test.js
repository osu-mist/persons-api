
import chai from 'chai';
import chaiAsPromised from 'chai-as-promised';
import _ from 'lodash';
import proxyquire from 'proxyquire';
import sinon from 'sinon';

import { logger } from 'utils/logger';
import { fakeOsuId, fakeJobId } from './mock-data';
// import { createDaoProxy } from './test-helpers';

chai.should();
chai.use(chaiAsPromised);

describe('Test jobs-dao', () => {
  const daoPath = '../../db/oracledb/jobs-dao';

  sinon.stub(logger, 'error').returns(null);

  const createDaoProxy = (dbReturn) => {
    const executeStub = sinon.stub();
    executeStub.returns({ rows: [{}] });
    executeStub.onCall(0).returns(dbReturn);
    return proxyquire(daoPath, {
      './connection': {
        getConnection: sinon.stub().resolves({
          execute: executeStub,
          close: () => null,
          commit: () => null,
          rollback: () => null,
        }),
      },
    });
  };

  const laborDistExpected = { laborDistribution: [{}] };
  const testCases = [
    {
      message: 'getJobByJobId should return single result',
      functionName: 'getJobByJobId',
      dbReturn: { rows: [{}] },
      expected: laborDistExpected,
    },
    {
      message: 'getJobs should return multiple results',
      functionName: 'getJobs',
      dbReturn: { rows: [{}, {}] },
      expected: [laborDistExpected, laborDistExpected],
    },
  ];
  _.forEach(testCases, ({
    message,
    functionName,
    dbReturn,
    expected,
  }) => {
    it(message, () => {
      const daoProxy = createDaoProxy(dbReturn);
      const result = daoProxy[functionName](fakeOsuId, fakeJobId);
      return result.should.eventually.be.fulfilled.and.deep.equal(expected);
    });
  });

  const errorCases = [
    {
      message: 'getJobByJobId should throw error when multiple results are returned',
      functionName: 'getJobByJobId',
      dbReturn: { rows: [{}, {}] },
      expected: `Multiple job records found for job ID ${fakeJobId}`,
    },
  ];
  _.forEach(errorCases, ({
    message,
    functionName,
    dbReturn,
    expected,
  }) => {
    it(message, () => {
      const daoProxy = createDaoProxy(dbReturn);
      const result = daoProxy[functionName](fakeOsuId, fakeJobId);
      return result.should.eventually.be.rejectedWith(expected);
    });
  });

  sinon.restore();
});
