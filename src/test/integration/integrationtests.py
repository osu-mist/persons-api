import unittest
import sys
from utils import load_config, parse_args, request_by_id, request_by_query


class TestStringMethods(unittest.TestCase):

    def test_id(self):
        # expect 400 if identifiers are included together
        self.assertEqual(request_by_query({'osuID': osu_id, 'onid': onid}).status_code, 400)
        self.assertEqual(request_by_query({'osuID': osu_id, 'osuUID': osuuid}).status_code, 400)
        self.assertEqual(request_by_query({'onid': onid, 'osuUID': osuuid}).status_code, 400)
        self.assertEqual(request_by_query({'osuID': osu_id, 'onid': onid, 'osuUID': osuuid}).status_code, 400)

        # expect 200 if there is only one identifier and valid
        osu_id_res = request_by_query({'osuID': osu_id})
        onid_res = request_by_query({'onid': onid})
        osuuid_res = request_by_query({'osuUID': osuuid})
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

    def test_osu_id(self):
        valid_res = request_by_id(osu_id)

        # expect 200 if osuID is valid
        self.assertEqual(valid_res.status_code, 200)

        # osuID and return data should matched
        self.assertIsNotNone(valid_res.json()['data'])
        self.assertEqual(valid_res.json()['data']['type'], 'person')
        self.assertEqual(valid_res.json()['data']['id'], osu_id)

        # expect 404 if osuID is not valid
        self.assertEqual(request_by_id('123456789').status_code, 404)


if __name__ == '__main__':
    args, argv = parse_args()
    person = load_config(args.inputfile)
    osu_id = person['osu_id']
    onid = person['onid']
    osuuid = person['osuuid']
    sys.argv[:] = argv
    unittest.main()
