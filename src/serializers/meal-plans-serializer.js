import { Serializer as JsonApiSerializer } from 'jsonapi-serializer';
import _ from 'lodash';

import { formatSubObjects } from 'utils/format-sub-objects';
import { serializerOptions } from 'utils/jsonapi';
import { openapi } from 'utils/load-openapi';
import { apiBaseUrl, resourcePathLink, paramsLink } from 'utils/uri-builder';

const planResourceProp = openapi.components.schemas.MealPlanResult.properties.data.properties;
const planResourceType = planResourceProp.type.enum[0];
const planResourceAttributes = planResourceProp.attributes.properties;
const planResourceKeys = _.keys(planResourceAttributes);

/**
 * Get serializer arguments for JsonApiSerializer
 *
 * @param {string} osuId
 * @returns {object} serializer arguments
 */
const getSerializerArgs = (osuId) => {
  const planResourcePath = `persons/${osuId}/${planResourceType}`;
  return {
    identifierField: 'mealPlanId',
    resourceKeys: planResourceKeys,
    resourcePath: planResourcePath,
    enableDataLinks: true,
  };
};

/**
 * Serializes raw data into JSON API format
 *
 * @param {object[]} rawMealPlans raw data from data source
 * @param {string} osuId OSU ID of a person
 * @param {object} query Query parameters from request
 * @returns {object[]} Serialized meal-plans
 */
const serializeMealPlans = (rawMealPlans, osuId, query) => {
  const serializerArgs = getSerializerArgs(osuId, query);
  const planResourceUrl = resourcePathLink(apiBaseUrl, serializerArgs.resourcePath);
  const topLevelSelfLink = paramsLink(planResourceUrl, query);
  serializerArgs.topLevelSelfLink = topLevelSelfLink;

  formatSubObjects(rawMealPlans);

  return new JsonApiSerializer(
    planResourceType,
    serializerOptions(serializerArgs, planResourceType, serializerArgs.topLevelSelfLink),
  ).serialize(rawMealPlans);
};

const serializeMealPlan = (rawMealPlan, osuId) => {
  const serializerArgs = getSerializerArgs(osuId);
  const planResourceUrl = resourcePathLink(apiBaseUrl, serializerArgs.resourcePath);
  const topLevelSelfLink = resourcePathLink(planResourceUrl, rawMealPlan.mealPlanId);
  serializerArgs.topLevelSelfLink = topLevelSelfLink;

  formatSubObjects([rawMealPlan]);

  return new JsonApiSerializer(
    planResourceType,
    serializerOptions(serializerArgs, planResourceType, serializerArgs.topLevelSelfLink),
  ).serialize(rawMealPlan);
};

export { serializeMealPlans, serializeMealPlan };
