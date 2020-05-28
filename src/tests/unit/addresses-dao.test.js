import chai from 'chai';
import chaiAsPromised from 'chai-as-promised';
// import _ from 'lodash';
import sinon from 'sinon';

import { logger } from 'utils/logger';
import { fakeOsuId } from './mock-data';
import { createDaoProxy } from './test-helpers';

chai.should();
chai.use(chaiAsPromised);

describe('Test addresses-dao', () => {
  const daoPath = '../../db/oracledb/addresses-dao';

  sinon.stub(logger, 'error').returns(null);

  it('getAddressesByInternalId should return multiple results', () => {
    const daoProxy = createDaoProxy(daoPath, { rows: [{}, {}] });
    const result = daoProxy.getAddressesByInternalId(fakeOsuId, {});
    return result.should.eventually.be.fulfilled.and.deep.equal([{}, {}]);
  });

  sinon.restore();
});
