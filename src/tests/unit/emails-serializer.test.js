import chai from 'chai';

import { serializeEmails } from 'serializers/emails-serializer';
import { rawEmail, fakeOsuId } from './mock-data';
import { testMultipleResources } from './test-helpers';

chai.should();

describe('Test emails-serializer', () => {
  it('serializeEmails should properly serialize multiple email data in JSON API format', () => {
    const result = serializeEmails([rawEmail, rawEmail], fakeOsuId);
    return testMultipleResources(result);
  });
});
