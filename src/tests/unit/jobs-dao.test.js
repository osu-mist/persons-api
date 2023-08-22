import chai from 'chai';
import chaiAsPromised from 'chai-as-promised';
import _ from 'lodash';
import proxyquire from 'proxyquire';
import sinon from 'sinon';

import { contrib } from '../../db/oracledb/contrib/contrib';
import { fakeOsuId, fakeJobId, jobStubType } from './mock-data';
import { daoBeforeEach, assertStubCalled } from './test-helpers';

chai.should();
chai.use(chaiAsPromised);

describe('Test jobs-dao', () => {
  const daoPath = '../../db/oracledb/jobs-dao';

  beforeEach(daoBeforeEach);
  afterEach(() => sinon.restore());

  /**
   * createDaoProxy creates a proxy DAO using an array of results. dbReturns specifies the result
   * for the specific call index. The last entry of dbReturns is repeated for calls beyond
   * what is specifed in dbReturns.
   *
   * @param {object[]} dbReturns an array of results
   * @returns {object} a proxy DAO
   */
  const createDaoProxy = (dbReturns) => {
    const executeStub = sinon.stub();

    executeStub.returns(dbReturns[dbReturns.length - 1]);
    _.forEach(dbReturns, (dbReturn, i) => {
      executeStub.onCall(i).returns(dbReturn);
    });

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
      const daoProxy = createDaoProxy([dbReturn, { rows: [{}] }]);
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
      const daoProxy = createDaoProxy([dbReturn, { rows: [{}] }]);
      const result = daoProxy[functionName](fakeOsuId, fakeJobId);
      return result.should.eventually.be.rejectedWith(expected);
    });
  });

  const handleJobTestCases = [
    {
      message: 'handleJob should call graduateJob without studentEmployeeInd',
      dbReturn: { rows: [{ count: 1 }], outBinds: { result: null } },
      expected: null,
      postBody: { changeReason: { code: 'dummy' } },
      expectedCalledType: jobStubType.graduateJob,
    },
    {
      message: 'handleJob should call graduateJob',
      dbReturn: { rows: [{ count: 1 }], outBinds: { result: null } },
      expected: null,
      postBody: { studentEmployeeInd: false, changeReason: { code: 'dummy' } },
      expectedCalledType: jobStubType.graduateJob,
    },
    {
      message: 'handleJob should call studentJob',
      dbReturn: { rows: [{ count: 1 }], outBinds: { result: null } },
      expected: null,
      postBody: { studentEmployeeInd: true, positionNumber: 'C50236', changeReason: { code: 'dummy' } },
      expectedCalledType: jobStubType.studentJob,
    },
    {
      message: 'handleJob should call terminateJob for student',
      dbReturn: { rows: [{ count: 1 }], outBinds: { result: null } },
      expected: null,
      postBody: { studentEmployeeInd: true, positionNumber: 'C50236', changeReason: { code: 'TERMJ' } },
      expectedCalledType: jobStubType.terminateJob,
    },
    {
      message: 'handleJob should call terminateJob for graduate',
      dbReturn: { rows: [{ count: 1 }], outBinds: { result: null } },
      expected: null,
      postBody: { studentEmployeeInd: false, positionNumber: 'C50236', changeReason: { code: 'TERMJ' } },
      expectedCalledType: jobStubType.terminateJob,
    },
    {
      message: 'handleJob should call updateLaborChangeJob for graduate',
      dbReturn: { rows: [{ count: 1 }], outBinds: { result: null } },
      expected: null,
      postBody: { studentEmployeeInd: false, positionNumber: 'C50236', changeReason: { code: 'NONE' } },
      expectedCalledType: jobStubType.updateLaborChangeJob,
    },
    {
      message: 'handleJob should call updateLaborChangeJob for student',
      dbReturn: { rows: [{ count: 1 }], outBinds: { result: null } },
      expected: null,
      postBody: { studentEmployeeInd: true, positionNumber: 'C50236', changeReason: { code: 'NONE' } },
      expectedCalledType: jobStubType.updateLaborChangeJob,
    },
  ];
  _.forEach(handleJobTestCases, ({
    message,
    dbReturn,
    expected,
    postBody,
    expectedCalledType,
  }) => {
    it(message, async () => {
      const stubGraduateJob = sinon.stub(contrib, 'graduateJob');
      const stubStudentJob = sinon.stub(contrib, 'studentJob');
      const stubTerminateJob = sinon.stub(contrib, 'terminateJob');
      const stubUpdateLaborChangeJob = sinon.stub(contrib, 'updateLaborChangeJob');
      const daoProxy = createDaoProxy([dbReturn]);

      const result = daoProxy.handleJob(fakeOsuId, postBody);
      await result.should.eventually.be.fulfilled.and.deep.equal(expected);

      assertStubCalled(expectedCalledType === jobStubType.graduateJob, stubGraduateJob);
      assertStubCalled(expectedCalledType === jobStubType.studentJob, stubStudentJob);
      assertStubCalled(expectedCalledType === jobStubType.terminateJob, stubTerminateJob);
      assertStubCalled(
        expectedCalledType === jobStubType.updateLaborChangeJob, stubUpdateLaborChangeJob,
      );
    });
  });

  const handleJobErrorCases = [
    {
      message: 'handleJob returns error for student with invalid positionNumber and valid reason code',
      dbReturn: { rows: [{ count: 1 }], outBinds: { result: null } },
      postBody: { studentEmployeeInd: true, positionNumber: 'X50236', changeReason: { code: 'TERME' } },
      expected: 'Valid position numbers for termination must begin with one of these prefixes: C50, C51, C52, C60, C69',
    },
    {
      message: 'handleJob returns error for student with invalid positionNumber and invalid reason code',
      dbReturn: { rows: [{ count: 1 }], outBinds: { result: null } },
      postBody: { studentEmployeeInd: true, positionNumber: 'X50236', changeReason: { code: 'dummy' } },
      expected: 'Student position numbers must begin with one of these prefixes: C50, C51, C52',
    },
    {
      message: 'handleJob returns error for graduate with invalid reason code',
      dbReturn: { rows: [{ count: 0 }], outBinds: { result: null } },
      postBody: { studentEmployeeInd: false, positionNumber: 'C50236', changeReason: { code: 'dummy' } },
      expected: 'Invalid change reason code dummy',
    },
    {
      message: 'handleJob returns error for graduate with termination code',
      dbReturn: { rows: [{ count: 1 }], outBinds: { result: ['JTRM'] } },
      postBody: { studentEmployeeInd: false, positionNumber: 'C50236', changeReason: { code: 'dummy' } },
      expected: 'JTRM',
    },
  ];
  _.forEach(handleJobErrorCases, ({
    message,
    dbReturn,
    postBody,
    expected,
  }) => {
    it(message, () => {
      const daoProxy = createDaoProxy([dbReturn]);
      const result = daoProxy.handleJob(fakeOsuId, postBody);

      result.should
        .eventually.be.fulfilled
        .and.be.instanceOf(Error)
        .and.have.property('message', expected);
    });
  });

  it('handleJob returns error for graduate with non-termination code', () => {
    const expected = 'ZZZZ';
    const dbReturn = { rows: [{ count: 1 }], outBinds: { result: [expected] } };
    const postBody = { studentEmployeeInd: false, positionNumber: 'C50236', changeReason: { code: 'dummy' } };

    const daoProxy = createDaoProxy([dbReturn]);
    const result = daoProxy.handleJob(fakeOsuId, postBody);

    result.should
      .eventually.be.rejected
      .and.be.instanceOf(Error)
      .and.have.property('message', expected);
  });
});
