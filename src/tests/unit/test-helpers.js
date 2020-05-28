import proxyquire from 'proxyquire';
import sinon from 'sinon';

/**
 * Creates sinon stub for oracledb connection
 *
 * @param {object} dbReturn value to be returned by connection.execute()
 * @returns {Promise<object>} sinon stub for connection
 */
const getConnectionStub = (dbReturn) => sinon.stub().resolves({
  execute: () => dbReturn,
  close: () => null,
  commit: () => null,
  rollback: () => null,
});

/**
 * Creates a proxy for the dao file being tested
 *
 * @param {string} daoPath relative path to dao file
 * @param {object} dbReturn value to be returned by connection.execute()
 */
const createDaoProxy = (daoPath, dbReturn) => proxyquire(daoPath, {
  './connection': {
    getConnection: getConnectionStub(dbReturn),
  },
});

export { createDaoProxy, getConnectionStub };
