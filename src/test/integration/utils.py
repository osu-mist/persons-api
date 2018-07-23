import argparse
import json
import unittest
import requests
import sys


def parse_args():
    parser = argparse.ArgumentParser()
    parser.add_argument('-i', help='input file', dest='inputfile')
    ns, args = parser.parse_known_args(namespace=unittest)
    return ns, sys.argv[:1] + args


def load_config(input_file):
    global api_url, headers

    config = json.load(open(input_file))
    api_url = config["hostname"] + config["version"] + config['api']
    payload = {
        'client_id': config["client_id"],
        'client_secret': config["client_secret"],
        'grant_type': 'client_credentials'
    }
    res = requests.post(config["token_api_url"], data=payload).json()
    headers = {'Authorization': 'Bearer ' + res["access_token"]}
    config_data = {
        'api_url': api_url,
        'ids_person': config['ids_person'],
        'jobs_person': config['jobs_person'],
        'no_job_person': config['no_job_person'],
        'phones_person': config['phones_person'],
        'long_phone_person': config['long_phone_person'],
        'regular_name': config['regular_name'],
        'fuzzy_name': config['fuzzy_name'],
        'alias_names': config['alias_names'],
        'old_name': config['old_name'],
        'old_id_person': config['old_id_person'],
        'meal_plan_person': config['meal_plan_person']
    }
    return config_data


def get_person_by_ids(params):
    global api_url, headers
    return requests.get(api_url, headers=headers, params=params)


def get_person_by_osu_id(osu_id):
    global api_url, headers
    return requests.get(api_url + osu_id, headers=headers)


def get_jobs_by_osu_id(osu_id):
    global api_url, headers
    return requests.get(api_url + osu_id + '/jobs', headers=headers)


def get_image_by_osu_id(osu_id, params=None):
    global api_url, headers
    return requests.get(api_url + osu_id + '/image',
                        headers=headers, params=params)


def get_meal_plans_by_osu_id(osu_id):
    global api_url, headers
    return requests.get(api_url + osu_id + '/meal-plans', headers=headers)


def get_meal_plan_by_id(osu_id, meal_plan_id):
    global api_url, headers
    return requests.get(api_url + osu_id + '/meal-plans/' + meal_plan_id,
                        headers=headers)
