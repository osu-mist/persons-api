/* eslint-disable no-unused-vars */
import { Serializer as JsonApiSerializer } from 'jsonapi-serializer';
import _ from 'lodash';

import { serializerOptions } from 'utils/jsonapi';
import { openapi } from 'utils/load-openapi';
import { apiBaseUrl, resourcePathLink, paramsLink } from 'utils/uri-builder';
import { contrib } from '../db/oracledb/contrib/contrib';

const jobResourceProp = openapi.components.schemas.JobResult.properties.data.properties;
const jobResourceType = jobResourceProp.type.enum[0];
const jobResourceAttributes = jobResourceProp.attributes.allOf;
const jobCombinedAttributes = _.merge(jobResourceAttributes[0], jobResourceAttributes[1]);
const jobResourceKeys = _.keys(jobCombinedAttributes.properties);
const jobResourcePath = 'jobs';
const jobResourceUrl = resourcePathLink(apiBaseUrl, jobResourcePath);


const serializeJobs = (rawJobs, query) => {
  const topLevelSelfLink = paramsLink(apiBaseUrl, query);
  const serializerArgs = {
    identifierField: 'osuId',
    resourceKeys: jobResourceKeys,
    resourcePath: jobResourcePath,
    topLevelSelfLink,
    enableDataLinks: true,
  };

  return new JsonApiSerializer(
    jobResourceType,
    serializerOptions(serializerArgs, jobResourcePath, topLevelSelfLink),
  ).serialize(rawJobs);
};

export { serializeJobs };
