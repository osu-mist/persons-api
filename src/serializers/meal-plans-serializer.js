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
 * @param {object} query
 * @returns {object} serializer arguments
 */
const getSerializerArgs = (osuId, query) => {
  const planResourcePath = `persons/${osuId}/${planResourceType}`;
  const planResourceUrl = resourcePathLink(apiBaseUrl, planResourcePath);
  const topLevelSelfLink = paramsLink(planResourceUrl, query);
  return {
    identifierField: 'mealPlanId',
    resourceKeys: planResourceKeys,
    resourcePath: planResourcePath,
    topLevelSelfLink,
    enableDataLinks: true,
  };
};

const serializeMealPlans = (rawMealPlans, osuId, query) => {
  const serializerArgs = getSerializerArgs(osuId, query);

  formatSubObjects(rawMealPlans);

  return new JsonApiSerializer(
    planResourceType,
    serializerOptions(serializerArgs, planResourceType, serializerArgs.topLevelSelfLink),
  ).serialize(rawMealPlans);
};

export { serializeMealPlans };
