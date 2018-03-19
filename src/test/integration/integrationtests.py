import unittest
import sys
from utils import load_config, parse_args, request_by_id, request_by_query


class TestStringMethods(unittest.TestCase):

    def test_id(self):
        osu_id_res = request_by_query({'osuID': valid_osu_id})
        onid_res = request_by_query({'onid': valid_onid})
        osuuid_res = request_by_query({'osuUID': valid_osuuid})
        self.assertEqual(osu_id_res.status_code, 200)
        self.assertEqual(onid_res.status_code, 200)
        self.assertEqual(osuuid_res.status_code, 200)

    def test_osu_id(self):
        valid_res = request_by_id(valid_osu_id)
        not_exist_res = request_by_id('123456789')

        self.assertEqual(valid_res.status_code, 200)
        self.assertIsNotNone(valid_res.json()['data'])
        self.assertEqual(not_exist_res.status_code, 404)


if __name__ == '__main__':
    args, argv = parse_args()
    valid_person = load_config(args.inputfile)
    valid_osu_id = valid_person['osu_id']
    valid_onid = valid_person['onid']
    valid_osuuid = valid_person['osuuid']
    sys.argv[:] = argv
    unittest.main()
