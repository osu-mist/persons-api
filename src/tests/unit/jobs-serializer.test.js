import chai from 'chai';
import _ from 'lodash';

import { serializeJob, serializeJobs } from 'serializers/jobs-serializer';
import { rawJob, fakeOsuId } from './mock-data';
import { testSingleResource, testMultipleResources } from './test-helpers';

chai.should();

describe('Test jobs-serializer', () => {
  it('serialzeJob should return data in proper JSON API format', () => {
    const result = serializeJob(_.cloneDeep(rawJob), fakeOsuId);
    return testSingleResource(result, 'jobs');
  });

  it('serializeJobs should return data in proper JSON API format', () => {
    const result = serializeJobs([_.cloneDeep(rawJob), _.cloneDeep(rawJob)], fakeOsuId, {});
    return testMultipleResources(result);
  });
});
