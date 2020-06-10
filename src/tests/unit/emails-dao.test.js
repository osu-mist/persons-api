import chai from 'chai';
import chaiAsPromised from 'chai-as-promised';
import sinon from 'sinon';

import { logger } from 'utils/logger';
import { fakeOsuId } from './mock-data';
import { createDaoProxy } from './test-helpers';

chai.should();
chai.use(chaiAsPromised);

describe('Test emails-dao', () => {
  const daoPath = '../../db/oracledb/emails-dao';

  sinon.stub(logger, 'error').returns(null);

  sinon.restore();

  it('getEmailsByOsuId should return multiple results', () => {
    const daoProxy = createDaoProxy(daoPath, { rows: [{}, {}] });
    const result = daoProxy.getEmailsByOsuId(fakeOsuId, {});
    return result.should.eventually.be.fulfilled.and.deep.equal([{}, {}]);
  });
});
