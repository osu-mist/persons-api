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
    job.classifiedInd = job.classifiedInd === 'Y';
    job.employmentType = job['employeeClassification.code'] === 'XA' ? 'student' : 'graduate';

    job['contractType.description'] = contrib.getContractTypeDescrByCode(job['contractType.code']);
    job['i9Form.description'] = contrib.getI9FormDescrByCode(job['i9Form.code']);
    job['status.description'] = contrib.getEmployeeStatusDescrByCode(job['status.code']);
    job['employeeClassification.category'] = contrib.getClassificationCategoryByCode(
      job['employeeClassification.code'],
    );

    // oracle aliases have a character limit of 30 so we set the correct name here
    const nameConversion = [
      { converted: 'timesheet.predecessor.description', alias: 'timesheet.pred.description' },
      { converted: 'homeOrganization.current.description', alias: 'homeOrganization.current.desc' },
      { converted: 'homeOrganization.predecessor.code', alias: 'homeOrganization.pred.code' },
      { converted: 'homeOrganization.predecessor.description', alias: 'homeOrganization.pred.desc' },
      { converted: 'employeeClassification.shortDescription', alias: 'employeeClass.shortDesc' },
      { converted: 'employeeClassification.longDescription', alias: 'employeeClass.longDesc' },
    ];
    _.forEach(nameConversion, ({ converted, alias }) => {
      job[converted] = job[alias];
      delete job[alias];
    });
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

/**
 * Serializes raw job data
 *
 * @param {object[]} rawJobs raw job data from data source
 * @param {string} osuId OSU ID of a person
 * @param {object} query query parameters passed in with request
 * @returns {object[]} Serialized job data
 */
const serializeJobs = (rawJobs, osuId, query) => {
  const serializerArgs = getSerializerArgs(osuId, query);

  prepareRawData(rawJobs);

  return new JsonApiSerializer(
    jobResourceType,
    serializerOptions(serializerArgs, jobResourceType, serializerArgs.topLevelSelfLink),
  ).serialize(rawJobs);
};

export { serializeJobs };
