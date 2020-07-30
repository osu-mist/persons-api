import chai from 'chai';
import proxyquire from 'proxyquire';

import { rawEmail, fakeOsuId } from './mock-data';
import { testMultipleResources, createConfigStub } from './test-helpers';

chai.should();

describe('Test emails-serializer', () => {
  const configStub = createConfigStub();
  const { serializeEmails } = proxyquire(
    'serializers/emails-serializer',
    {},
  );
  configStub.restore();

  it('serializeEmails should properly serialize multiple email data in JSON API format', () => {
    const result = serializeEmails([rawEmail, rawEmail], fakeOsuId);
    return testMultipleResources(result);
  });
});
