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
        """Test case: GET /persons/{id}"""

        nullable_fields = self.get_nullable_fields('PersonResource')

        valid_osu_ids = self.test_cases['valid_osu_ids']
        invalid_osu_ids = self.test_cases['invalid_osu_ids']

        for osu_id in valid_osu_ids:
            resource = 'PersonResource'
            self.check_endpoint(f'{endpoint}/{osu_id}', resource, 200, nullable_fields=nullable_fields)

        for osu_id in invalid_osu_ids:
            resource = 'ErrorObject'
            self.check_endpoint(f'{endpoint}/{osu_id}', resource, 404, nullable_fields=nullable_fields)


if __name__ == '__main__':
    arguments, argv = utils.parse_arguments()

    # Setup logging level
    if arguments.debug:
        logging.basicConfig(level=logging.DEBUG)
    else:
        logging.basicConfig(level=logging.INFO)

    IntegrationTests.setup(arguments.config_path, arguments.openapi_path)
    unittest.main(argv=argv)
