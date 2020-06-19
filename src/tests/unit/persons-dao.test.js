import chai from 'chai';
import chaiAsPromised from 'chai-as-promised';
import _ from 'lodash';
import sinon from 'sinon';

import { fakeOsuId } from './mock-data';
import { createDaoProxy, daoBeforeEach } from './test-helpers';

chai.should();
chai.use(chaiAsPromised);

describe('Test persons-dao', () => {
  const daoPath = '../../db/oracledb/persons-dao';

  beforeEach(daoBeforeEach);
  afterEach(() => sinon.restore());

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
      const daoProxy = createDaoProxy(daoPath, dbReturn);
      const result = daoProxy[functionName]({ fakeOsuId });
      return result.should.eventually.be.fulfilled.and.deep.equal(expected);
    });
  });

  it('createPerson should throw an error when querying the newly created person returns undefined',
    () => {
      const daoProxy = createDaoProxy(daoPath, {
        outBinds: {
          outId: fakeOsuId,
        },
        rows: [undefined],
      });
      const result = daoProxy.createPerson({});
      return result.should.eventually.be.rejectedWith('Person creation failed');
    });

  it('createPerson should return error when outId contains ERROR', () => {
    const daoProxy = createDaoProxy(daoPath, { outBinds: { outId: 'ERROR: something happened' } });
    const result = daoProxy.createPerson({});
    return result.should.eventually.be.an('error');
  });
});
