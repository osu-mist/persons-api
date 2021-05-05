import chai from 'chai';
import chaiAsPromised from 'chai-as-promised';
import _ from 'lodash';
import sinon from 'sinon';

import { createDaoProxy, getConnectionStub, daoBeforeEach } from './test-helpers';

chai.should();
chai.use(chaiAsPromised);

describe('Test addresses-dao', () => {
  const daoPath = '../../db/oracledb/addresses-dao';

  beforeEach(daoBeforeEach);
  afterEach(() => sinon.restore());

  const testCases = [
    {
      message: 'getAddressesByInternalId should return multiple results',
      functionName: 'getAddressesByInternalId',
      dbReturn: { rows: [{}, {}] },
      expected: [{}, {}],
    },
    {
      message: 'hasSameAddressType should return single result on success',
      functionName: 'hasSameAddressType',
      dbReturn: { rows: [{}] },
      expected: {},
    },
    {
      message: 'phoneHasSameAddressType should return single result on success',
      functionName: 'phoneHasSameAddressType',
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
    it(message, async () => {
      const daoProxy = createDaoProxy(daoPath, dbReturn);
      const connectionStub = await getConnectionStub(dbReturn)();
      const result = daoProxy[functionName](connectionStub, { addressType: {} });
      return result.should.eventually.be.fulfilled.and.deep.equal(expected);
    });
  });

  const errorCases = [
    {
      message: 'hasSameAddressType should throw error when rows is greater than 1',
      functionName: 'hasSameAddressType',
      dbReturn: { rows: [{}, {}] },
      expected: 'Multiple addresses found for the same address type undefined for [object Object]',
    },
    {
      message: 'phoneHasSameAddressType should throw error when rows is greater than 1',
      functionName: 'phoneHasSameAddressType',
      dbReturn: { rows: [{}, {}] },
      expected: 'Multiple phone records found for the address type undefined for [object Object]',
    },
  ];
  _.forEach(errorCases, ({
    message,
    functionName,
    dbReturn,
    expected,
  }) => {
    it(message, async () => {
      const daoProxy = createDaoProxy(daoPath, dbReturn);
      const connectionStub = await getConnectionStub(dbReturn)();
      const result = daoProxy[functionName](connectionStub, { addressType: {} });
      return result.should.eventually.be.rejectedWith(expected);
    });
  });
});
