import chai from 'chai';
import chaiAsPromised from 'chai-as-promised';
import proxyquire from 'proxyquire';
import sinon from 'sinon';

import { logger } from 'utils/logger';

chai.should();
chai.use(chaiAsPromised);

describe('Test persons-dao', () => {
  sinon.stub(logger, 'error').returns(null);

  const fakeOsuId = '999999999';

  const createDaoProxy = () => proxyquire('db/oracledb/persons-dao', {
    './connection': {
      getConnection: sinon.stub().resolves({
        execute: () => ({ rows: [{}] }),
        close: () => null,
        commit: () => null,
      }),
    },
  });

  it('getPersonById should return single result', async () => {
    const daoProxy = createDaoProxy();
    const result = daoProxy.getPersonById(fakeOsuId);
    result.should.eventually.be.fulfilled.and.deep.equal({});
  });
});
