import chai from 'chai';
import chaiAsPromised from 'chai-as-promised';
import _ from 'lodash';
import sinon from 'sinon';

import { fakeOsuId, fakePhoneBody } from './mock-data';
import { createDaoProxy, daoBeforeEach } from './test-helpers';

chai.should();
chai.use(chaiAsPromised);

describe('Test phones-dao', () => {
  const daoPath = '../../db/oracledb/phones-dao';

  beforeEach(daoBeforeEach);
  afterEach(() => sinon.restore());

  const testCases = [
    {
      message: 'getPhonesByInternalId should return multiple results',
      functionName: 'getPhonesByInternalId',
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
      const daoProxy = createDaoProxy(daoPath, dbReturn);
      const result = daoProxy[functionName](fakeOsuId, _.clone(fakePhoneBody));
      return result.should.eventually.be.fulfilled.and.deep.equal(expected);
    });
  });
});
