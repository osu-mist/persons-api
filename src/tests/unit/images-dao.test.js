import chai from 'chai';
import chaiAsPromised from 'chai-as-promised';
import _ from 'lodash';
import sinon from 'sinon';

import { fakeOsuId } from './mock-data';
import { createDaoProxy, daoBeforeEach } from './test-helpers';

chai.should();
chai.use(chaiAsPromised);

describe('Test images-dao', () => {
  const daoPath = '../../db/oracledb/images-dao';

  beforeEach(daoBeforeEach);
  afterEach(() => sinon.restore());

  const testCases = [
    {
      message: 'getImageById should return image data on success',
      dbReturn: { rows: [{ image: 'image data' }] },
      expected: 'image data',
    },
    {
      message: 'getImageById should return undefined when rows is undefined',
      dbReturn: {},
      expected: undefined,
    },
    {
      message: 'getImageById should return undefined when rows is empty',
      dbReturn: { rows: [] },
      expected: undefined,
    },
  ];
  _.forEach(testCases, ({ message, dbReturn, expected }) => {
    it(message, () => {
      const daoProxy = createDaoProxy(daoPath, dbReturn);
      const result = daoProxy.getImageById(fakeOsuId);
      return result.should.eventually.be.fulfilled.and.deep.equal(expected);
    });
  });
});
