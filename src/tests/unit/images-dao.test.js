import chai from 'chai';
import chaiAsPromised from 'chai-as-promised';
import _ from 'lodash';
import proxyquire from 'proxyquire';
import sinon from 'sinon';

import { logger } from 'utils/logger';
import { fakeOsuId } from './mock-data';

chai.should();
chai.use(chaiAsPromised);

describe('Test images-dao', () => {
  sinon.stub(logger, 'error').returns(null);

  sinon.restore();
});
