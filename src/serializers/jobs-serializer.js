import { Serializer as JsonApiSerializer } from 'jsonapi-serializer';
import _ from 'lodash';

import { contrib } from 'db/oracledb/contrib/contrib';
import { formatSubObjects } from 'utils/format-sub-objects';
import { serializerOptions } from 'utils/jsonapi';
import { openapi } from 'utils/load-openapi';
import { apiBaseUrl, resourcePathLink, paramsLink } from 'utils/uri-builder';

const jobResourceProp = openapi.components.schemas.JobResult.properties.data.properties;
const jobResourceType = jobResourceProp.type.enum[0];
const jobResourceAttributes = jobResourceProp.attributes.allOf;
const jobCombinedAttributes = _.merge(jobResourceAttributes[0], jobResourceAttributes[1]);
const jobResourceKeys = _.keys(jobCombinedAttributes.properties);

const prepareRawData = (rawJobs) => {
  _.forEach(rawJobs, (job) => {
    job.jobId = `${job.positionNumber}-${job.suffix}`;
    job.accruesLeaveInd = job.accruesLeaveInd === 'Y';
    job.employmentType = job['employeeClassification.code'] === 'XA' ? 'student' : 'graduate';

    job['contractType.description'] = contrib.getContractTypeDescrByCode(job['contractType.code']);
    job['i9Form.description'] = contrib.getI9FormDescrByCode(job['i9Form.code']);
    job['status.description'] = contrib.getEmployeeStatusDescrByCode(job['status.code']);

    // oracle aliases have a character limit of 30 so we set the correct name here
    job['timesheet.predecessor.description'] = job['timesheet.pred.description'];
    delete job['timesheet.pred.description'];
    job['homeOrganization.current.description'] = job['homeOrganization.current.desc'];
    delete job['homeOrganization.current.desc'];
    job['homeOrganization.predecessor.code'] = job['homeOrganization.pred.code'];
    delete job['homeOrganization.pred.code'];
    job['homeOrganization.predecessor.description'] = job['homeOrganization.pred.desc'];
    delete job['homeOrganization.pred.desc'];
    job['employeeClassification.shortDescription'] = job['employeeClass.shortDesc'];
    job['employeeClassification.longDescription'] = job['employeeClass.longDesc'];
    delete job['employeeClass.shortDesc'];
    delete job['employeeClass.longDesc'];
  });

  formatSubObjects(rawJobs);
};

const getSerializerArgs = (osuId, query) => {
  const jobResourcePath = `persons/${osuId}/${jobResourceType}`;
  const jobResourceUrl = resourcePathLink(apiBaseUrl, jobResourcePath);
  const topLevelSelfLink = paramsLink(jobResourceUrl, query);
  return {
    identifierField: 'jobId',
    resourceKeys: jobResourceKeys,
    resourcePath: jobResourcePath,
    topLevelSelfLink,
    enableDataLinks: true,
  };
};

const serializeJobs = (rawJobs, osuId, query) => {
  const serializerArgs = getSerializerArgs(osuId, query);

  prepareRawData(rawJobs);

  return new JsonApiSerializer(
    jobResourceType,
    serializerOptions(serializerArgs, jobResourceType, serializerArgs.topLevelSelfLink),
  ).serialize(rawJobs);
};

export { serializeJobs };
