import proxyquire from 'proxyquire';
import sinon from 'sinon';

const createDaoProxy = (daoPath, dbReturn) => proxyquire(daoPath, {
  './connection': {
    getConnection: sinon.stub().resolves({
      execute: () => dbReturn,
      close: () => null,
      commit: () => null,
      rollback: () => null,
    }),
  },
});

export { createDaoProxy };
