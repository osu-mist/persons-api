import chai from 'chai';
import chaiAsPromised from 'chai-as-promised';
import _ from 'lodash';
import proxyquire from 'proxyquire';
import sinon from 'sinon';

import { logger } from 'utils/logger';
import { fakeOsuId } from './mock-data';

chai.should();
chai.use(chaiAsPromised);

describe('Test persons-dao', () => {
  sinon.stub(logger, 'error').returns(null);

  const createDaoProxy = (dbReturn) => proxyquire('db/oracledb/persons-dao', {
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
      message: 'getPersonById should return single result',
      functionName: 'getPersonById',
      expected: {},
      dbReturn: { rows: [{}] },
    },
    {
      message: 'personExists should return ID when person exists',
      functionName: 'personExists',
      expected: fakeOsuId,
      dbReturn: { rows: [{ internalId: fakeOsuId }] },
    },
    {
      message: 'personExists should return null for non-existent person',
      functionName: 'personExists',
      expected: null,
      dbReturn: { rows: {} },
    },
    {
      message: 'createPerson should return person data on success',
      functionName: 'createPerson',
      expected: { firstName: 'sally' },
      dbReturn: { outBinds: { outId: fakeOsuId }, rows: [{ firstName: 'sally' }] },
    },
  ];
  _.forEach(testCases, ({
    message,
    functionName,
    expected,
    dbReturn,
  }) => {
    it(message, () => {
      const daoProxy = createDaoProxy(dbReturn);
      const result = daoProxy[functionName]({ fakeOsuId });
      return result.should.eventually.be.fulfilled.and.deep.equal(expected);
    });
  });

  it('createPerson should throw an error when outId contains an error', () => {
    const daoProxy = createDaoProxy({ outBinds: { outId: fakeOsuId }, rows: [null] });
    const result = daoProxy.createPerson({});
    return result.should.eventually.be.rejectedWith('Person creation failed');
  });

  it('createPerson should return error when outId contains ERROR', () => {
    const daoProxy = createDaoProxy({ outBinds: { outId: 'ERROR: something happened' } });
    const result = daoProxy.createPerson({});
    return result.should.eventually.be.a('error');
  });

  sinon.restore();
});
