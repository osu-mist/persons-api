import unittest
import sys
from utils import load_config, parse_args, request_by_id, request_by_query


class TestStringMethods(unittest.TestCase):

    def test_id(self):
        self.assertEqual(request_by_query({'osuID': osu_id}).status_code, 200)
        self.assertEqual(request_by_query({'onid': onid}).status_code, 200)
        self.assertEqual(request_by_query({'osuUID': osuuid}).status_code, 200)
        self.assertEqual(request_by_query({'osuID': osu_id, 'onid': onid}).status_code, 400)
        self.assertEqual(request_by_query({'osuID': osu_id, 'osuUID': osuuid}).status_code, 400)
        self.assertEqual(request_by_query({'onid': onid, 'osuUID': osuuid}).status_code, 400)
        self.assertEqual(request_by_query({'osuID': osu_id, 'onid': onid, 'osuUID': osuuid}).status_code, 400)

    def test_osu_id(self):
        valid_res = request_by_id(osu_id)

        self.assertEqual(valid_res.status_code, 200)
        self.assertIsNotNone(valid_res.json()['data'])
        self.assertEqual(request_by_id('123456789').status_code, 404)


if __name__ == '__main__':
    args, argv = parse_args()
    person = load_config(args.inputfile)
    osu_id = person['osu_id']
    onid = person['onid']
    osuuid = person['osuuid']
    sys.argv[:] = argv
    unittest.main()
