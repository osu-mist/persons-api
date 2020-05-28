import chai from 'chai';
import chaiAsPromised from 'chai-as-promised';
// import _ from 'lodash';
import sinon from 'sinon';


import { logger } from 'utils/logger';
import { fakeOsuId } from './mock-data';
// import { createDaoProxy } from './test-helpers';

chai.should();
chai.use(chaiAsPromised);

describe('Test persons-dao', () => {
  sinon.stub(logger, 'error').returns(null);

  console.log(fakeOsuId);

  sinon.restore();
});
