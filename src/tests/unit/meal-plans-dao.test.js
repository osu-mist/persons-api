import chai from 'chai';
import chaiAsPromised from 'chai-as-promised';
import _ from 'lodash';
import proxyquire from 'proxyquire';
import sinon from 'sinon';

import { logger } from 'utils/logger';
import { fakeOsuId, fakeMealPlanId } from './mock-data';

chai.should();
chai.use(chaiAsPromised);

describe('Test meal-plans-dao', () => {
  sinon.stub(logger, 'error').returns(null);

  const createDaoProxy = (dbReturn) => proxyquire('db/oracledb/meal-plans-dao', {
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
      message: 'getMealPlanByMealPlanId should return a single result',
      functionName: 'getMealPlanByMealPlanId',
      dbReturn: { rows: [{}, {}] },
      expected: {},
    },
    {
      message: 'getMealPlanByMealPlanId should return undefined when rows is empty',
      functionName: 'getMealPlanByMealPlanId',
      dbReturn: { rows: [] },
      expected: undefined,
    },
    {
      message: 'getMealPlansByOsuId should return multiple results',
      functionName: 'getMealPlansByOsuId',
      dbReturn: { rows: [{}, {}] },
      expected: [{}, {}],
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
      const result = daoProxy[functionName](fakeOsuId, fakeMealPlanId);
      return result.should.eventually.be.fulfilled.and.deep.equal(expected);
    });
  });

  sinon.restore();
});
