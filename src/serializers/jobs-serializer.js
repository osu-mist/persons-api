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
  const shouldBeFloat = [
    'fullTimeEquivalency',
    'hourlyRate',
    'hoursPerPay',
    'salary.annual',
    'salary.assignment',
    'earningCode.hours',
  ];
  const shouldBeNumber = [
    'appointmentPercent',
    'salary.paysPerYear',
    'salary.step',
  ];

  _.forEach(rawJobs, (job) => {
    job.jobId = `${job.positionNumber}-${job.suffix}`;
    job.accruesLeaveInd = job.accruesLeaveInd === 'Y';
    job.classifiedInd = job.classifiedInd === 'Y';
    job.studentEmployeeInd = job['employeeClassification.code'] === 'XA';

    job['contractType.description'] = contrib.getContractTypeDescrByCode(job['contractType.code']);
    job['i9Form.description'] = contrib.getI9FormDescrByCode(job['i9Form.code']);
    job['status.description'] = contrib.getEmployeeStatusDescrByCode(job['status.code']);
    job['employeeClassification.category'] = contrib.getClassificationCategoryByCode(
      job['employeeClassification.code'],
    );

    _.forEach(shouldBeFloat, (field) => {
      job[field] = parseFloat(job[field]);
    });
    _.forEach(shouldBeNumber, (field) => {
      job[field] = Number(job[field]);
    });

    _.forEach(job.laborDistribution, (laborDistribution) => {
      laborDistribution.distributionPercent = parseFloat(laborDistribution.distributionPercent);
    });

    // oracle aliases have a character limit of 30 so we set the correct name here
    const nameConversion = [
      { converted: 'timesheet.predecessor.description', alias: 'timesheet.pred.description' },
      { converted: 'homeOrganization.current.description', alias: 'homeOrganization.current.desc' },
      { converted: 'homeOrganization.predecessor.code', alias: 'homeOrganization.pred.code' },
      { converted: 'homeOrganization.predecessor.description', alias: 'homeOrganization.pred.desc' },
      { converted: 'employeeClassification.shortDescription', alias: 'employeeClass.shortDesc' },
      { converted: 'employeeClassification.longDescription', alias: 'employeeClass.longDesc' },
      { converted: 'employerIdentification.description', alias: 'employerId.description' },
    ];
    _.forEach(nameConversion, ({ converted, alias }) => {
      job[converted] = job[alias];
      delete job[alias];
    });
  });

  formatSubObjects(rawJobs);
};

/**
 * Returns serializer arguments for serializing job records
 *
 * @param {string} osuId OSU ID of a person
 * @param {object} query Query parameters passed in with request
 * @returns {object} Serializer arguments
 */
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

/**
 * Serializes a single raw job data
 *
 * @param {object} rawJob Single raw job from data source
 * @param {*} osuId OSU ID of a person
 * @returns {object[]} Serialized job data
 */
const serializeJob = (rawJob, osuId) => {
  const serializerArgs = getSerializerArgs(osuId);

  prepareRawData([rawJob]);
  serializerArgs.topLevelSelfLink = `${serializerArgs.topLevelSelfLink}/${rawJob.jobId}`;

  return new JsonApiSerializer(
    jobResourceType,
    serializerOptions(serializerArgs, jobResourceType, serializerArgs.topLevelSelfLink),
  ).serialize(rawJob);
};

/**
 * Serialize bodies sent with post or put requests
 *
 * @param {string} osuId OSU ID of a person
 * @param {object} body Body passed in with request
 * @returns {object} Serialized job data
 */
const serializePostOrPatch = (osuId, body) => {
  const { topLevelSelfLink } = getSerializerArgs(osuId);
  const links = { self: `${topLevelSelfLink}/${body.data.id}` };
  body.links = links;
  body.data.links = links;

  return body;
};

export { serializeJobs, serializeJob, serializePostOrPatch };
