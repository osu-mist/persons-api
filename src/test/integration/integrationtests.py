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
        self.assertEqual(utils.get_person_by_ids({'osuID': osu_id, 'onid': onid}).status_code, 400)
        self.assertEqual(utils.get_person_by_ids({'osuID': osu_id, 'osuUID': osuuid}).status_code, 400)
        self.assertEqual(utils.get_person_by_ids({'onid': onid, 'osuUID': osuuid}).status_code, 400)
        self.assertEqual(utils.get_person_by_ids({'osuID': osu_id, 'onid': onid, 'osuUID': osuuid}).status_code, 400)

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
        ids_list = [
            (person_data['id'], osu_id),
            (person_data['attributes']['username'], onid),
            (person_data['attributes']['osuUID'], osuuid)
        ]
        for res_id, param_id in ids_list:
            self.assertEqual(res_id, param_id)
            self.assertIsInstance(res_id, str)

        # should return the same person if queried by each identifier
        self.assertTrue(osu_id_res.content == onid_res.content == osuuid_res.content)

    def test_phones(self):
        phones_res = utils.get_person_by_osu_id(phones_osu_id)

        home_phone = phones_res.json()['data']['attributes']['homePhone']
        alternate_phone = phones_res.json()['data']['attributes']['alternatePhone']
        primary_phone = phones_res.json()['data']['attributes']['primaryPhone']
        mobile_phone = phones_res.json()['data']['attributes']['mobilePhone']

        phones = filter(None, [home_phone, alternate_phone, primary_phone, mobile_phone])
        for phone in phones:
            # validate E.164 phone format
            parsed_phone = self.__parse_phone_number(phone)
            e164_phone = phonenumbers.format_number(parsed_phone, phonenumbers.PhoneNumberFormat.E164)
            self.assertEqual(phone, e164_phone)

    # the backend data source has some bad phone numbers.
    # a known bad phone number should be unformatted,
    # therefore unable to be parsed.
    def test_bad_phones(self):
        long_phone_person = utils.get_person_by_osu_id(long_phone_osu_id) 
        self.assertEqual(long_phone_person.status_code, 200)
        long_home_phone = long_phone_person.json()['data']['attributes']['homePhone']

        with self.assertRaises(phonenumbers.phonenumberutil.NumberParseException):
            self.__parse_phone_number(long_home_phone)

    @staticmethod
    def __parse_phone_number(phone_number):
        return phonenumbers.parse(phone_number, 'None')

    def test_date(self):
        iso_8601_full_date_pattern = re.compile(r'^\d{4}-\d{2}-\d{2}')
        person_res = utils.get_person_by_osu_id(osu_id)
        jobs_res = utils.get_jobs_by_osu_id(jobs_osu_id)

        dates = [person_res.json()['data']['attributes']['birthDate']]
        for job in jobs_res.json()['data']['attributes']['jobs']:
            dates += [job['beginDate'], job['endDate']]

        for date in filter(None, dates):
            # validate ISO 8601 full-date format
            self.assertIsNotNone(iso_8601_full_date_pattern.match(date).group(0))

    def test_person_by_osu_id(self):
        person_res = utils.get_person_by_osu_id(osu_id)

        # expect 200 if osuID is valid
        self.assertEqual(person_res.status_code, 200)

        # osuID and return data should matched
        self.assertIsNotNone(person_res.json()['data'])
        self.assertEqual(person_res.json()['data']['type'], 'person')
        self.assertEqual(person_res.json()['data']['id'], osu_id)

        # expect 404 if osuID is not valid
        self.assertEqual(utils.get_person_by_osu_id(not_valid_osu_id).status_code, 404)

    def test_jobs_by_osu_id(self):
        jobs_res = utils.get_jobs_by_osu_id(jobs_osu_id)
        no_job_res = utils.get_jobs_by_osu_id(no_job_osu_id)

        # expect 200 if osuID is valid
        for res, res_osu_id in [(jobs_res, jobs_osu_id), (no_job_res, no_job_osu_id)]:
            self.assertEqual(res.status_code, 200)

            self.assertIsNotNone(res.json()['data'])
            self.assertEqual(res.json()['data']['type'], 'jobs')
            self.assertEqual(res.json()['data']['id'], res_osu_id)

        # test person with jobs
        self.assertGreater(len(jobs_res.json()['data']['attributes']['jobs']), 0)

        # test person without job
        self.assertEqual(len(no_job_res.json()['data']['attributes']['jobs']), 0)

        # expect 404 if osuID is not valid
        self.assertEqual(utils.get_person_by_osu_id(not_valid_osu_id).status_code, 404)

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
        rms = math.sqrt(reduce(operator.add, map(lambda x, y: (x - y) ** 2, h1, h2)) / len(h1))
        self.assertLess(rms, 100)

        # expect 400 if width out of range (1 - 2000)
        self.assertEqual(utils.get_image_by_osu_id(osu_id, {'width': 0}).status_code, 400)
        self.assertEqual(utils.get_image_by_osu_id(osu_id, {'width': 2001}).status_code, 400)

        # test image resize
        resize_width = 500
        resize_res = utils.get_image_by_osu_id(osu_id, {'width': resize_width})
        resized_image = Image.open(BytesIO(resize_res.content))

        original_res = utils.get_image_by_osu_id(osu_id)
        original_image = Image.open(BytesIO(original_res.content))

        expected_height = ((original_image.height * resize_width) / original_image.width)
        self.assertEqual(resize_res.status_code, 200)
        self.assertEqual(expected_height, resized_image.height)
        self.assertEqual(resize_width, resized_image.width)

        # expect 404 if osuID is not valid
        self.assertEqual(not_found_res.status_code, 404)

        # test content type
        self.assertEqual(image_res.headers['Content-Type'], 'image/jpeg')
        self.assertEqual(not_found_res.headers['Content-Type'], 'application/json')
        self.assertEqual(bad_res.headers['Content-Type'], 'application/json')

    def test_self_link(self):
        person_ids_res = utils.get_person_by_ids({'osuID': osu_id})
        for person in person_ids_res.json()['data']:
            self.assertEqual(person['links']['self'], api_url + osu_id)

        person_id_res = utils.get_person_by_osu_id(osu_id)
        self.assertEqual(person_id_res.json()['data']['links']['self'], api_url + osu_id)

        jobs_res = utils.get_jobs_by_osu_id(jobs_osu_id)
        self.assertEqual(jobs_res.json()['data']['links']['self'], api_url + osu_id + '/jobs')


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

    sys.argv[:] = argv
    unittest.main()
