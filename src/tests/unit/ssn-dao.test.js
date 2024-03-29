import chai from 'chai';
import chaiAsPromised from 'chai-as-promised';
import _ from 'lodash';
import sinon from 'sinon';

import { fakeOsuId } from './mock-data';
import { createDaoProxy, daoBeforeEach } from './test-helpers';

chai.should();
chai.use(chaiAsPromised);

describe('Test ssn-dao', () => {
  const daoPath = '../../db/oracledb/ssn-dao';

  beforeEach(daoBeforeEach);
  afterEach(() => sinon.restore());

  const testCases = [
    {
      message: 'ssnIsNotNull should return true when ssn exists',
      functionName: 'ssnIsNotNull',
      dbReturn: { rows: [{ ssnStatus: 'Y' }] },
      expected: true,
    },
    {
      message: 'ssnIsNotNull should return false when ssn does not exist',
      functionName: 'ssnIsNotNull',
      dbReturn: { rows: [] },
      expected: false,
    },
    {
      message: 'createSsn should return undefined on success',
      functionName: 'createSsn',
      dbReturn: { rows: [{ ssnStatus: 'valid' }] },
      expected: undefined,
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
      const result = daoProxy[functionName](fakeOsuId, {});
      return result.should.eventually.be.fulfilled.and.equal(expected);
    });
  });

  const errorCases = [
    {
      message: 'createSsn should throw error when more than one record is returned',
      dbReturn: { rows: [{}, {}] },
    },
    {
      message: 'createSsn should throw error when ssn status is not valid',
      dbReturn: { rows: [{ ssnStatus: 'bad' }] },
    },
  ];
  _.forEach(errorCases, ({ message, dbReturn }) => {
    it(message, () => {
      const daoProxy = createDaoProxy(daoPath, dbReturn);
      const result = daoProxy.createSsn(fakeOsuId, {});
      return result.should.eventually.be.rejectedWith('Error occurred creating SSN');
    });
  });
});
