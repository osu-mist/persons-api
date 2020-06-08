"""Integration tests"""
import json
import logging
import unittest
import yaml

import utils


class IntegrationTests(utils.UtilsTestCase):
    """Integration tests class"""

    @classmethod
    def setup(cls, config_path, openapi_path):
        """Performs basic setup"""

        with open(config_path) as config_file:
            config = json.load(config_file)
            cls.base_url = utils.setup_base_url(config)
            cls.session = utils.setup_session(config)
            cls.test_cases = config['test_cases']
            cls.local_test = config['local_test']
            cls.query_params = config['query_params']

        with open(openapi_path) as openapi_file:
            openapi = yaml.load(openapi_file, Loader=yaml.SafeLoader)
            if 'swagger' in openapi:
                backend = 'flex'
            elif 'openapi' in openapi:
                backend = 'openapi-spec-validator'
            else:
                exit('Error: could not determine openapi document version')

        cls.openapi = openapi

    @classmethod
    def tearDownClass(cls):
        cls.session.close()


    def test_get_person_by_id(self, endpoint='/persons'):
        """Test case: GET /persons/{osuId}"""

        nullable_fields = self.get_nullable_fields('PersonResource')

        valid_person_ids = self.test_cases['valid_person_ids']
        invalid_osu_ids = self.test_cases['invalid_osu_ids']

        for person in valid_person_ids:
            resource = 'PersonResource'
            osu_id = person['osu_id']
            self.check_endpoint(
                f'{endpoint}/{osu_id}',
                resource,
                200,
                nullable_fields=nullable_fields
            )

        for osu_id in invalid_osu_ids:
            resource = 'ErrorObject'
            self.check_endpoint(
                f'{endpoint}/{osu_id}',
                resource,
                404,
                nullable_fields=nullable_fields
            )

    def test_get_job_by_id(self, endpoint='/jobs'):
        """Test case: GET /persons/{osuId}/jobs/{jobId}"""

        nullable_fields = self.get_nullable_fields('JobResource')

        valid_person_ids = self.test_cases['valid_person_ids']
        invalid_job_ids = self.test_cases['invalid_job_ids']

        for person in valid_person_ids:
            resource = 'JobResource'
            osu_id = person['osu_id']
            job_id = person['job_id']
            self.check_endpoint(
                f'/persons/{osu_id}{endpoint}/{job_id}',
                resource,
                200,
                nullable_fields=nullable_fields,
            )

        osu_id = invalid_job_ids['osu_id']
        for job_id in invalid_job_ids['job_ids']:
            self.check_endpoint(
                f'/persons/{osu_id}{endpoint}/{job_id}',
                'ErrorObject',
                404,
                nullable_fields=nullable_fields
            )

    def test_get_jobs(self, endpoint='/jobs'):
        """Test case: GET /persons/{osuId}/jobs"""

        nullable_fields = self.get_nullable_fields('JobResource')

        valid_person_ids = self.test_cases['valid_person_ids']

        resource = 'JobResource'
        for person in valid_person_ids:
            osu_id = person['osu_id']
            self.check_endpoint(
                f'/persons/{osu_id}{endpoint}',
                resource,
                200,
                nullable_fields=nullable_fields
            )

        query_params = self.query_params['jobs']
        osu_id = self.query_params['osu_id']
        self.check_query_params(
            f'/persons/{osu_id}{endpoint}',
            resource,
            nullable_fields,
            query_params,
            osu_id
        )

    def test_get_images(self, endpoint='/images'):
        """Test case: GET /persons/{osuId}/images"""

        valid_person_ids = self.test_cases['valid_person_ids']
        for person in valid_person_ids:
            osu_id = person['osu_id']

            # check_endpoint assumes response will be in JSON format which is not true for images
            requested_url = f'{self.base_url}/persons/{osu_id}{endpoint}'
            response = self.session.get(requested_url)
            status_code = response.status_code
            self.assertEqual(
                status_code,
                200,
                f'requested_url: {requested_url},\nresponse_body: {response}'
            )

    def test_get_meal_plans(self, endpoint='/meal-plans'):
        """Test case: GET /persons/{osuId}/meal-plans"""

        resource = 'MealPlanResource'
        nullable_fields = self.get_nullable_fields(resource)

        valid_meal_plan_ids = self.test_cases['valid_meal_plan_ids']

        for meal_plan in valid_meal_plan_ids:
            osu_id = meal_plan['osu_id']
            self.check_endpoint(
                f'/persons/{osu_id}{endpoint}',
                resource,
                200,
                nullable_fields=nullable_fields
            )

        query_params = self.query_params['meal_plans']
        osu_id = self.query_params['osu_id']
        self.check_query_params(
            f'/persons/{osu_id}{endpoint}',
            resource,
            nullable_fields,
            query_params,
            osu_id
        )

    def test_get_meal_plans_by_meal_plan_id(self, endpoint='/meal-plans'):
        """Test case: GET /persons/{osuId}/meal-plans/{mealPlanId}"""

        resource = 'MealPlanResource'
        nullable_fields = self.get_nullable_fields(resource)

        valid_meal_plan_ids = self.test_cases['valid_meal_plan_ids']
        for meal_plan in valid_meal_plan_ids:
            osu_id = meal_plan['osu_id']
            meal_plan_id = meal_plan['meal_plan_id']
            self.check_endpoint(
                f'/persons/{osu_id}{endpoint}/{meal_plan_id}',
                resource,
                200,
                nullable_fields=nullable_fields
            )

        invalid_meal_plan_ids = self.test_cases['invalid_meal_plan_ids']
        osu_id = invalid_meal_plan_ids['osu_id']
        for meal_plan_id in invalid_meal_plan_ids['meal_plan_ids']:
            self.check_endpoint(
                f'/persons/{osu_id}{endpoint}/{meal_plan_id}',
                'ErrorObject',
                400,
                nullable_fields=nullable_fields
            )

    def test_get_addresses(self, endpoint='/addresses'):
        """Test case: GET /persons/{osuId}/addresses"""

        resource = 'AddressResource'
        nullable_fields = self.get_nullable_fields(resource)

        valid_person_ids = self.test_cases['valid_person_ids']
        for person in valid_person_ids:
            osu_id = person['osu_id']
            self.check_endpoint(
                f'/persons/{osu_id}{endpoint}',
                resource,
                200,
                nullable_fields=nullable_fields
            )

        query_params = self.query_params['addresses']
        osu_id = self.query_params['osu_id']
        self.check_query_params(
            f'/persons/{osu_id}{endpoint}',
            resource,
            nullable_fields,
            query_params,
            osu_id
        )

    def test_get_phones(self, endpoint='/phones'):
        """Test case: GET /persons/{osuId}/phones"""

        resource = 'PhoneResource'
        nullable_fields = self.get_nullable_fields(resource)

        valid_person_ids = self.test_cases['valid_person_ids']
        for person in valid_person_ids:
            osu_id = person['osu_id']
            self.check_endpoint(
                f'/persons/{osu_id}{endpoint}',
                resource,
                200,
                nullable_fields=nullable_fields
            )

        query_params = self.query_params['phones']
        osu_id = self.query_params['osu_id']
        self.check_query_params(
            f'/persons/{osu_id}{endpoint}',
            resource,
            nullable_fields,
            query_params,
            osu_id
        )


if __name__ == '__main__':
    arguments, argv = utils.parse_arguments()

    # Setup logging level
    if arguments.debug:
        logging.basicConfig(level=logging.DEBUG)
    else:
        logging.basicConfig(level=logging.INFO)

    IntegrationTests.setup(arguments.config_path, arguments.openapi_path)
    unittest.main(argv=argv)
