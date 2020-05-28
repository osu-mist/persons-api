import chai from 'chai';
import chaiAsPromised from 'chai-as-promised';
import _ from 'lodash';
import proxyquire from 'proxyquire';
import sinon from 'sinon';

import { logger } from 'utils/logger';
import { createDaoProxy, getConnectionStub } from './test-helpers';
import { fakeOsuId } from './mock-data';

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

  it('createAddress should throw error when multiple new addresses are found', () => {
    // need unique dao proxy for this
    const executeStub = sinon.stub().returns({ outBinds: { seqno: {} }, rows: [] });
    // third call with execute is the one we need to test
    executeStub.onCall(3).returns({ rows: [{}, {}] });
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
    const result = daoProxy.createAddress(fakeOsuId, { addressType: {} });
    return result.should.eventually
      .be.rejectedWith('Error: Multiple active addresses for address type undefined');
  });

  sinon.restore();
});
