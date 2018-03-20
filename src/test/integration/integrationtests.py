import math
import operator
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

        # identifiers and return data should matched
        self.assertIsNotNone(osu_id_res.json()['data'])
        self.assertEqual(osu_id_res.json()['data'][0]['type'], 'person')
        self.assertEqual(osu_id_res.json()['data'][0]['id'], osu_id)
        self.assertEqual(onid_res.json()['data'][0]['attributes']['username'], onid)
        self.assertEqual(onid_res.json()['data'][0]['attributes']['osuUID'], osuuid)

        # should return the same person if queried by each identifier
        self.assertTrue(osu_id_res.text == onid_res.text == osuuid_res.text)

    def test_person_by_osu_id(self):
        valid_res = utils.get_person_by_osu_id(osu_id)

        # expect 200 if osuID is valid
        self.assertEqual(valid_res.status_code, 200)

        # osuID and return data should matched
        self.assertIsNotNone(valid_res.json()['data'])
        self.assertEqual(valid_res.json()['data']['type'], 'person')
        self.assertEqual(valid_res.json()['data']['id'], osu_id)

        # expect 404 if osuID is not valid
        self.assertEqual(utils.get_person_by_osu_id(not_valid_osu_id).status_code, 404)

    def test_jobs_by_osu_id(self):
        valid_res = utils.get_jobs_by_osu_id(osu_id)

        # expect 200 if osuID is valid
        self.assertEqual(valid_res.status_code, 200)

        self.assertIsNotNone(valid_res.json()['data'])
        self.assertEqual(valid_res.json()['data']['type'], 'jobs')
        self.assertEqual(valid_res.json()['data']['id'], osu_id)

        # expect 404 if osuID is not valid
        self.assertEqual(utils.get_person_by_osu_id(not_valid_osu_id).status_code, 404)

    def test_image_by_osu_id(self):
        valid_res = utils.get_image_by_osu_id(osu_id)

        # expect 200 if osuID is valid
        self.assertEqual(valid_res.status_code, 200)

        # test default image
        expected_image = Image.open('images/defaultimage.jpg')
        response_image = Image.open(BytesIO(valid_res.content))
        h1 = expected_image.histogram()
        h2 = response_image.histogram()
        rms = math.sqrt(reduce(operator.add, map(lambda x, y: (x - y) ** 2, h1, h2)) / len(h1))
        self.assertLess(rms, 100)

        # expect 400 if width out of range (1 - 2000)
        # self.assertEqual(utils.get_image_by_osu_id(osu_id, {'width': 0}.status_code), 400)
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
        self.assertEqual(utils.get_person_by_osu_id(not_valid_osu_id).status_code, 404)


if __name__ == '__main__':
    args, argv = utils.parse_args()
    person = utils.load_config(args.inputfile)
    osu_id = person['osu_id']
    onid = person['onid']
    osuuid = person['osuuid']
    not_valid_osu_id = '123456789'
    sys.argv[:] = argv
    unittest.main()
