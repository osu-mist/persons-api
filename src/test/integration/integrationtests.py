import math
import operator
import phonenumbers
import re
import sys
import unittest
import utils
from functools import reduce
from io import BytesIO
from PIL import Image


class TestStringMethods(unittest.TestCase):
    def test_person_by_ids(self):
        # expect 400 if identifiers are included together
        self.assertEqual(
            utils.get_person_by_ids({
                'osuID': osu_id,
                'onid': onid
            }).status_code, 400)
        self.assertEqual(
            utils.get_person_by_ids({
                'osuID': osu_id,
                'osuUID': osuuid
            }).status_code, 400)
        self.assertEqual(
            utils.get_person_by_ids({
                'onid': onid,
                'osuUID': osuuid
            }).status_code, 400)
        self.assertEqual(
            utils.get_person_by_ids({
                'osuID': osu_id,
                'onid': onid,
                'osuUID': osuuid
            }).status_code, 400)

        # expect 200 if there is only one identifier and valid
        osu_id_res = utils.get_person_by_ids({'osuID': osu_id})
        onid_res = utils.get_person_by_ids({'onid': onid})
        osuuid_res = utils.get_person_by_ids({'osuUID': osuuid})
        self.assertEqual(osu_id_res.status_code, 200)
        self.assertEqual(onid_res.status_code, 200)
        self.assertEqual(osuuid_res.status_code, 200)

        # test person data
        self.assertIsNotNone(osu_id_res.json()['data'])
        self.assertEqual(osu_id_res.json()['data'][0]['type'], 'person')

        # identifiers and return ids should matched
        person_data = osu_id_res.json()['data'][0]
        ids_list = [(person_data['id'], osu_id),
                    (person_data['attributes']['username'], onid),
                    (person_data['attributes']['osuUID'], osuuid)]
        for res_id, param_id in ids_list:
            self.assertEqual(res_id, param_id)
            self.assertIsInstance(res_id, str)

        # should return the same person if queried by each identifier
        self.assertTrue(
            osu_id_res.content == onid_res.content == osuuid_res.content)

    def test_phones(self):
        phones_res = utils.get_person_by_osu_id(phones_osu_id)
        phones_res_attributes = phones_res.json()['data']['attributes']

        home_phone = phones_res_attributes['homePhone']
        alternate_phone = phones_res_attributes['alternatePhone']
        primary_phone = phones_res_attributes['primaryPhone']
        mobile_phone = phones_res_attributes['mobilePhone']

        phones = filter(
            None, [home_phone, alternate_phone, primary_phone, mobile_phone])
        for phone in phones:
            # validate E.164 phone format
            parsed_phone = self.__parse_phone_number(phone)
            e164_phone = phonenumbers.format_number(
                parsed_phone, phonenumbers.PhoneNumberFormat.E164)
            self.assertEqual(phone, e164_phone)

    # the backend data source has some bad phone numbers.
    # a known bad phone number should be unformatted,
    # therefore unable to be parsed.
    def test_bad_phones(self):
        long_phone_person = utils.get_person_by_osu_id(long_phone_osu_id)
        self.assertEqual(long_phone_person.status_code, 200)
        long_home_phone = long_phone_person.json()['data']['attributes'][
            'homePhone']

        with self.assertRaises(
                phonenumbers.phonenumberutil.NumberParseException):
            self.__parse_phone_number(long_home_phone)

    @staticmethod
    def __parse_phone_number(phone_number):
        return phonenumbers.parse(phone_number, 'None')

    def test_date(self):
        iso_8601_full_date_pattern = re.compile(r'^\d{4}-\d{2}-\d{2}')
        person_res = utils.get_person_by_osu_id(osu_id)
        jobs_res = utils.get_jobs_by_osu_id(jobs_osu_id)

        dates = [person_res.json()['data']['attributes']['birthDate']]
        for job in jobs_res.json()['data']:
            attributes = job['attributes']
            dates += [attributes['beginDate'], attributes['endDate']]

        for date in filter(None, dates):
            # validate ISO 8601 full-date format
            self.assertIsNotNone(
                iso_8601_full_date_pattern.match(date).group(0))

    def test_person_by_osu_id(self):
        person_res = utils.get_person_by_osu_id(osu_id)

        # expect 200 if osuID is valid
        self.assertEqual(person_res.status_code, 200)

        # osuID and return data should matched
        self.assertIsNotNone(person_res.json()['data'])
        self.assertEqual(person_res.json()['data']['type'], 'person')
        self.assertEqual(person_res.json()['data']['id'], osu_id)

        # expect 404 if osuID is not valid
        self.assertEqual(
            utils.get_person_by_osu_id(not_valid_osu_id).status_code, 404)

    def test_jobs_by_osu_id(self):
        jobs_res = utils.get_jobs_by_osu_id(jobs_osu_id)
        no_job_res = utils.get_jobs_by_osu_id(no_job_osu_id)

        # expect 200 if osuID is valid
        for res, res_osu_id in [(jobs_res, jobs_osu_id),
                                (no_job_res, no_job_osu_id)]:
            self.assertEqual(res.status_code, 200)

        # test person with jobs
        self.assertGreater(self.length_of_response(jobs_res), 0)

        # test person without job
        self.assertEqual(self.length_of_response(no_job_res), 0)

        # expect 404 if osuID is not valid
        self.assertEqual(
            utils.get_person_by_osu_id(not_valid_osu_id).status_code, 404)

    def test_image_by_osu_id(self):
        image_res = utils.get_image_by_osu_id(osu_id)
        bad_res = utils.get_image_by_osu_id(osu_id, {'width': 999999})
        not_found_res = utils.get_person_by_osu_id(not_valid_osu_id)

        # expect 200 if osuID is valid
        self.assertEqual(image_res.status_code, 200)

        # test default image
        expected_image = Image.open('images/defaultimage.jpg')
        response_image = Image.open(BytesIO(image_res.content))
        h1 = expected_image.histogram()
        h2 = response_image.histogram()
        rms = math.sqrt(
            reduce(operator.add, map(lambda x, y: (x - y)**2, h1, h2)) /
            len(h1))
        self.assertLess(rms, 100)

        # expect 400 if width out of range (1 - 2000)
        self.assertEqual(
            utils.get_image_by_osu_id(osu_id, {
                'width': 0
            }).status_code, 400)
        self.assertEqual(
            utils.get_image_by_osu_id(osu_id, {
                'width': 2001
            }).status_code, 400)

        # test image resize
        resize_width = 500
        resize_res = utils.get_image_by_osu_id(osu_id, {'width': resize_width})
        resized_image = Image.open(BytesIO(resize_res.content))

        original_res = utils.get_image_by_osu_id(osu_id)
        original_image = Image.open(BytesIO(original_res.content))

        expected_height = (
            (original_image.height * resize_width) / original_image.width)
        self.assertEqual(resize_res.status_code, 200)
        self.assertEqual(expected_height, resized_image.height)
        self.assertEqual(resize_width, resized_image.width)

        # expect 404 if osuID is not valid
        self.assertEqual(not_found_res.status_code, 404)

        # test content type
        self.assertEqual(image_res.headers['Content-Type'], 'image/jpeg')
        self.assertEqual(not_found_res.headers['Content-Type'],
                         'application/json')
        self.assertEqual(bad_res.headers['Content-Type'], 'application/json')

    def test_self_link(self):
        person_ids_res = utils.get_person_by_ids({'osuID': osu_id})
        for person in person_ids_res.json()['data']:
            self.assertEqual(person['links']['self'], api_url + osu_id)

        person_id_res = utils.get_person_by_osu_id(osu_id)
        self.assertEqual(person_id_res.json()['data']['links']['self'],
                         api_url + osu_id)

        jobs_res = utils.get_jobs_by_osu_id(jobs_osu_id)
        for job in jobs_res.json()['data']:
            attributes = job['attributes']
            position_number = attributes['positionNumber']
            suffix = attributes['suffix']
            expected_url = "{}{}/jobs?positionNumber={}&suffix={}".format(
                api_url, osu_id, position_number, suffix)

            self.assertEqual(job['links']['self'], expected_url)

    def test_name_search(self):
        person_res = utils.get_person_by_ids({
            'firstName': regular_name_person['first_name'],
            'lastName': regular_name_person['last_name']
        })

        self.assertEqual(person_res.status_code, 200)
        self.assertLess(person_res.elapsed.total_seconds(), 3)
        self.assertEqual(self.length_of_response(person_res), 1)

    def test_fuzzy_name_search(self):
        person_res = utils.get_person_by_ids({
            'firstName': fuzzy_name_person['first_name'],
            'lastName': fuzzy_name_person['last_name']
        })

        self.assertEqual(self.length_of_response(person_res), 1)

    def test_name_alias(self):
        for person_name in alias_persons:
            person_res = utils.get_person_by_ids({
                'firstName': person_name['first_name_alias'],
                'lastName': person_name['last_name']
            })

            person = person_res.json()['data'][0]['attributes']

            self.assertEqual(person['firstName'], person_name['first_name'])
            self.assertEqual(person['lastName'], person_name['last_name'])

    def test_bad_name_request(self):
        self.assertEqual(
            utils.get_person_by_ids({
                'firstName': 'foo'
            }).status_code, 400)
        self.assertEqual(
            utils.get_person_by_ids({
                'lastName': 'foo'
            }).status_code, 400)

    def test_old_name_search(self):
        parameters = {
            'firstName': old_name_person['first_name'],
            'lastName': old_name_person['last_name']
        }
        self.validate_old_parameters(parameters, 'searchOldNames')

    def test_old_osu_id_search(self):
        parameters = {'osuID': old_id_person}
        self.validate_old_parameters(parameters, 'searchOldOsuIDs')

    def validate_old_parameters(self, parameters, old_param):
        parameters[old_param] = False
        no_results = utils.get_person_by_ids(parameters)
        self.assertEqual(self.length_of_response(no_results), 0)

        parameters[old_param] = True
        person = utils.get_person_by_ids(parameters)
        self.assertEqual(self.length_of_response(person), 1)

    def test_meal_plan(self):
        meal_plan_response = utils.get_meal_plans_by_osu_id(meal_plan_person)
        self.assertEqual(meal_plan_response.status_code, 200)
        self.assertGreaterEqual(self.length_of_response(meal_plan_response), 1)

        for meal_plan in meal_plan_response.json()['data']:
            single_meal_plan_response = utils.get_meal_plan_by_id(
                    meal_plan_person, meal_plan['id'])
            self.assertEqual(single_meal_plan_response.status_code, 200)
            self.assertEqual(
                    meal_plan, single_meal_plan_response.json()['data'])

    def test_current_employee(self):
        employee = utils.get_person_by_osu_id(jobs_person).json()
        asserTrue(self.get_current_employee_field(employee))

        not_employee = utils.get_person_by_osu_id(no_job_osu_id).json()
        asserFalse(self.get_current_employee_field(not_employee))

    @staticmethod
    def get_current_employee_field(person):
        return person['data']['attributes']['currentEmployee']

    @staticmethod
    def length_of_response(response):
        return len(response.json()['data'])


if __name__ == '__main__':
    args, argv = utils.parse_args()
    config_data = utils.load_config(args.inputfile)
    api_url = config_data['api_url']

    not_valid_osu_id = '999999999'

    # person with ids
    osu_id = config_data['ids_person']['osu_id']
    onid = config_data['ids_person']['onid']
    osuuid = config_data['ids_person']['osuuid']

    # person with jobs
    jobs_osu_id = config_data['jobs_person']['osu_id']
    no_job_osu_id = config_data['no_job_person']['osu_id']

    # person with phones
    phones_osu_id = config_data['phones_person']['osu_id']

    # persons with bad phone data
    long_phone_osu_id = config_data['long_phone_person']['osu_id']

    # person with regular name
    regular_name_person = config_data['regular_name']

    # person with fuzzy name
    fuzzy_name_person = config_data['fuzzy_name']

    # persons with name aliases
    alias_persons = config_data['alias_names']

    # person with old name
    old_name_person = config_data['old_name']

    # person with old OSU ID
    old_id_person = config_data['old_id_person']['osu_id']

    # person with meal plan
    meal_plan_person = config_data['meal_plan_person']['osu_id']

    sys.argv[:] = argv
    unittest.main()
