import chai from 'chai';
import proxyquire from 'proxyquire';

import { rawMedical, fakeOsuId } from './mock-data';
import { testMultipleResources, createConfigStub } from './test-helpers';

chai.should();

describe('Test medical-serializer', () => {
  const configStub = createConfigStub();
  const { serializeMedical } = proxyquire(
    'serializers/medical-serializer',
    {},
  );
  configStub.restore();

  it('serializeMedical should properly serialize multiple medical data in JSON API format', () => {
    const result = serializeMedical([rawMedical, rawMedical], fakeOsuId);
    return testMultipleResources(result);
  });
});
