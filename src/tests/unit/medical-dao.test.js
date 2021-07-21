import chai from 'chai';
import chaiAsPromised from 'chai-as-promised';
import sinon from 'sinon';

import { fakeOsuId } from './mock-data';
import { createDaoProxy, daoBeforeEach } from './test-helpers';

chai.should();
chai.use(chaiAsPromised);

describe('Test medical-dao', () => {
  const daoPath = '../../db/oracledb/medical-dao';

  beforeEach(daoBeforeEach);
  afterEach(() => sinon.restore());

  it('getMedical should return multiple results', () => {
    const daoProxy = createDaoProxy(daoPath, { rows: [{}, {}] });
    const result = daoProxy.getMedical(fakeOsuId, {});
    return result.should.eventually.be.fulfilled.and.deep.equal([{}, {}]);
  });
});
