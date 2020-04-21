import config from 'config';
import _ from 'async-dash';
import oracledb from 'oracledb';

import { logger } from 'utils/logger';

const dbConfig = config.get('dataSources.oracledb');

process.on('SIGINT', () => process.exit());
oracledb.outFormat = oracledb.OBJECT;
oracledb.fetchAsString = [oracledb.DATE, oracledb.NUMBER];
oracledb.fetchAsBuffer = [oracledb.BLOB];

/** Increase 1 extra thread for every 5 pools but no more than 128 */
const threadPoolSize = dbConfig.poolMax + (dbConfig.poolMax / 5);
process.env.UV_THREADPOOL_SIZE = threadPoolSize > 128 ? 128 : threadPoolSize;

/** Connection pools */
const pools = {};

/**
 * Create a pool of connection
 *
 * @returns {Promise} Promise object represents a pool of connections
 */
const createPool = async () => {
  /** Attributes to use from config file */
  const attributes = ['connectString', 'user', 'password', 'poolMin', 'poolMax', 'poolIncrement'];
  const poolConfig = _.pick(dbConfig, attributes);
  await _.asyncEach(dbConfig.oracleSources, async (source) => {
    const connectConfig = _.pick(dbConfig[source], attributes);
    pools[source] = await oracledb.createPool({ ...connectConfig, ...poolConfig });
  });
};

/**
 * Get a connection from a created pool. Creates pool if it hasn't been created yet.
 *
 * @param {string} pool Name of the connection pool to use
 * @returns {Promise} Promise object represents a connection from created pool
 */
const getConnection = async (pool) => {
  if (!_.includes(dbConfig.oracleSources, pool)) {
    throw new Error(`Unknown pool name ${pool}`);
  } else if (!pools[pool]) {
    await createPool();
  }
  return pools[pool].getConnection();
};

/**
 * Validate database connection and throw an error if invalid
 *
 * @returns {Promise} resolves if database connection can be established and rejects otherwise
 */
const validateOracleDb = async () => {
  await _.asyncEach(dbConfig.oracleSources, async (source) => {
    let connection;
    try {
      connection = await getConnection(source);
      await connection.execute('SELECT 1 FROM DUAL');
    } catch (err) {
      logger.error(err);
      throw new Error(`Unable to connect to ${source} Oracle database`);
    } finally {
      if (connection) {
        await connection.close();
      }
    }
  });
};

export {
  getConnection, validateOracleDb,
};
