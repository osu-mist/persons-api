import chai from 'chai';
import chaiAsPromised from 'chai-as-promised';
import _ from 'lodash';
import sinon from 'sinon';

import { logger } from 'utils/logger';
import { createDaoProxy, getConnectionStub } from './test-helpers';

chai.should();
chai.use(chaiAsPromised);

describe('Test addresses-dao', () => {
  const daoPath = '../../db/oracledb/addresses-dao';

  sinon.stub(logger, 'error').returns(null);

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
    {
      message: 'createAddress should return single result',
      functionName: 'createAddress',
      dbReturn: { outBinds: { seqno: {} }, rows: [{}] },
      expected: { addrSeqno: {} },
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

  sinon.restore();
});
