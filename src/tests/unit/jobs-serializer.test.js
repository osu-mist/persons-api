import chai from 'chai';
import _ from 'lodash';
import proxyquire from 'proxyquire';

import { rawJob, fakeOsuId } from './mock-data';
import { testSingleResource, testMultipleResources, createConfigStub } from './test-helpers';

chai.should();

describe('Test jobs-serializer', () => {
  const configStub = createConfigStub();
  const { serializeJob, serializeJobs } = proxyquire(
    'serializers/jobs-serializer',
    {},
  );
  configStub.restore();

  it('serialzeJob should return data in proper JSON API format', () => {
    const result = serializeJob(_.cloneDeep(rawJob), fakeOsuId);
    return testSingleResource(result, 'jobs');
  });

  it('serializeJobs should return data in proper JSON API format', () => {
    const result = serializeJobs([_.cloneDeep(rawJob), _.cloneDeep(rawJob)], fakeOsuId, {});
    return testMultipleResources(result);
  });
});
