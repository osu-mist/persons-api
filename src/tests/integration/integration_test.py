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

        for job in invalid_job_ids:
            resource = 'ErrorObject'
            osu_id = job['osu_id']
            job_ids = job['job_ids']
            for job_id in job_ids:
                self.check_endpoint(
                    f'/persons/{osu_id}{endpoint}/{job_id}',
                    resource,
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
        for param in query_params:
            for value in query_params[param]['valid']:
                self.check_endpoint(
                    f'/persons/{osu_id}{endpoint}',
                    resource,
                    200,
                    nullable_fields=nullable_fields,
                    query_params={ param: value }
                )
            if 'invalid' in query_params[param]:
                for value in query_params[param]['invalid']:
                    self.check_endpoint(
                        f'/persons/{osu_id}{endpoint}',
                        'ErrorObject',
                        400,
                        nullable_fields=nullable_fields,
                        query_params={ param: value }
                    )

    def test_get_images(self, endpoint='/images'):
        """Test case: GET /persons/{osuId}/images"""

        valid_person_ids = self.test_cases['valid_person_ids']
        for person in valid_person_ids:
            osu_id = person['osu_id']
            self.check_endpoint(
                f'/persons/{osu_id}{endpoint}',
                'ImageResource',
                200
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
