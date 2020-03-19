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

/**
 * Prepare raw data from data source for the serializer
 *
 * @param {*} rawJobs rawJobs from data source
 */
const prepareRawJobs = (rawJobs) => {
  _.forEach(rawJobs, (job) => {
    const {
      contractCode,
      statusCode,
      changeReasonCode,
      changeReasonDesc,
      strsAssignmentCode,
      strsAssignmentDesc,
      i9FormCode,
      laborData,
    } = job;

    job.contractType = {
      code: contractCode,
      description: contrib.getContractDescByCode(contractCode),
    };

    job.status = {
      code: statusCode,
      description: contrib.getStatusDescByCode(statusCode),
    };

    job.changeReason = {
      code: changeReasonCode,
      description: changeReasonDesc,
    };

    job.strsAssignment = {
      code: strsAssignmentCode,
      description: strsAssignmentDesc,
    };

    job.supervisor = {
      osuId: job.supervisorId,
      firstName: job.supervisorFirstName,
      lastName: job.supervisorLastName,
      email: job.supervisorEmail,
      positionNumber: job.supervisorPositionNumber,
      suffix: job.supervisorSuffix,
    };

    job.timesheet = {
      current: {
        code: job.timesheetOrgCode,
        description: job.timesheetOrgDesc,
      },
      predecessor: {
        code: job.timesheetPredCode,
        description: job.timesheetPredDesc,
      },
    };

    job.homeOrganization = {
      current: {
        code: job.homeOrgCode,
        description: job.homeOrgDesc,
      },
      predecessor: {
        code: job.homeOrgPredCode,
        description: job.homeOrgPredDesc,
      },
    };

    job.salary = {
      annual: job.salaryAnnual,
      assignment: job.salaryAssignment,
      paysPerYear: job.paysPerYear,
      step: job.salaryStep,
      group: {
        code: job.salaryGroupCode,
        description: job.salaryGroupDesc,
      },
    };

    job.employeeClassification = {
      code: job.employeeClassCode,
      shortDescription: job.employeeClassShortDesc,
      longDescription: job.employeeClassLongDesc,
    };

    job.employerIdentification = {
      code: job.employerIdentificationCode,
      description: job.employerIdentificationDesc,
    };

    job.earningCode = {
      hours: job.earnCodeHours,
      shift: job.earnCodeShift,
      effectiveDate: job.earnCodeEffectiveDate,
      code: job.earnCode,
      shortDescription: job.earnCodeShortDesc,
      longDescription: job.earnCodeLongDesc,
    };

    job.i9Form = {
      code: i9FormCode,
      date: job.i9Date,
      expirationDate: job.i9ExpireDate,
      description: contrib.geti9FormDescByCode(i9FormCode),
    };

    job.laborDistribution = [];
    _.forEach(laborData, (labor) => {
      job.laborDistribution.push({
        effectiveDate: labor.effectiveDate,
        distributionPercent: labor.distributionPercent,
        accountIndex: {
          code: labor.accountIndexCode,
          description: labor.accountIndexDesc,
        },
        fund: {
          code: labor.fundCode,
          description: 'fillme',
        },
        organization: {
          code: labor.organizationCode,
          description: 'fillme',
        },
        account: {
          code: labor.accountCode,
          description: 'fillme',
        },
        program: {
          code: labor.programCode,
          description: 'fillme',
        },
        activity: {
          code: labor.activityCode,
          description: 'fillme',
        },
        location: {
          code: labor.locationCode,
          description: 'fillme',
        },
      });
    });

    // campusCode handle later
  });
};

/**
 * Serialize multiple jobs from data source
 *
 * @param {*} rawJobs
 * @param {*} query
 * @returns {object} Serialized job resource data
 */
const serializeJobs = (rawJobs, query) => {
  const topLevelSelfLink = paramsLink(apiBaseUrl, query);
  const serializerArgs = {
    identifierField: 'osuId',
    resourceKeys: jobResourceKeys,
    resourcePath: jobResourcePath,
    topLevelSelfLink,
    enableDataLinks: true,
  };

  // console.log(rawJobs[0]);
  prepareRawJobs(rawJobs);

  return new JsonApiSerializer(
    jobResourceType,
    serializerOptions(serializerArgs, jobResourcePath, topLevelSelfLink),
  ).serialize(rawJobs);
};

export { serializeJobs };
