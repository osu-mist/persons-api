import { expect } from 'chai';
import _ from 'lodash';
import proxyquire from 'proxyquire';
import sinon from 'sinon';

import { fakeBaseUrl, fakeOsuId } from './mock-data';

/**
 * Creates a proxy for the dao file being tested
 *
 * @param {string} daoPath relative path to dao file
 * @param {object} dbReturn value to be returned by connection.execute()
 * @returns {Promise<object>} stubbed oracledb connection object
 */
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

/**
 * Creates resource schema for expected test results
 *
 * @param {string} resourceType type of resource as named in openapi
 * @param {object} resourceAttributes fields expected in attributes subset of resourceType
 * @returns {object} expected schema of serialized resource
 */
const resourceSubsetSchema = (resourceType, resourceAttributes) => {
  const fakeUrl = `${fakeBaseUrl}/${resourceType}s/fakeOsuId`;
  const schema = {
    links: {
      self: fakeUrl,
    },
    data: {
      id: fakeOsuId,
      type: resourceType,
      links: { self: fakeUrl },
    },
  };
  if (resourceAttributes) {
    schema.data.attributes = resourceAttributes;
  }
  return schema;
};

/**
 * Helper function for lite-testing single resource
 *
 * @param {object} serializedResource serialized resource
 * @param {string} resourceType resource type
 * @param {object} nestedProps object containing properties nested under data.attributes
 */
const testSingleResource = (serializedResource, resourceType, nestedProps) => {
  expect(serializedResource).to.have.all.keys(resourceSubsetSchema(resourceType, nestedProps));

  if (nestedProps) {
    _.forEach(Object.keys(nestedProps), (prop) => {
      expect(serializedResource).to.have.nested.property(`data.attributes.${prop}`);
    });
  }
};

/**
 * Helper function for lite-testing multiple resources
 *
 * @param {object} serializedResources serialized resources
 * @returns {object} data object from serialized resources for further use
 */
const testMultipleResources = (serializedResources) => {
  const serializedResourcesData = serializedResources.data;
  expect(_.omit(serializedResources, 'meta')).to.have.keys('data', 'links');
  expect(serializedResourcesData).to.be.an('array');

  return serializedResourcesData;
};

export {
  createDaoProxy,
  testSingleResource,
  testMultipleResources,
};
