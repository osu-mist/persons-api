import argparse
import json
import sys
import unittest

import requests


def parse_args():
    parser = argparse.ArgumentParser()
    parser.add_argument('-i', help='input file', dest='inputfile')
    ns, args = parser.parse_known_args(namespace=unittest)
    return ns, sys.argv[:1] + args


def load_config(input_file):
    global api_url, session

    config = json.load(open(input_file))
    api_url = config["hostname"] + config["version"] + config['api']
    session = requests.Session()
    if config["use_basic_auth"]:
        session.verify = False
        session.auth = (
            config["basic_auth_username"], config["basic_auth_password"]
        )
    else:
        payload = {
            'client_id': config["client_id"],
            'client_secret': config["client_secret"],
            'grant_type': 'client_credentials'
        }
        res = requests.post(config["token_api_url"], data=payload).json()
        session.headers.update(
            {'Authorization': 'Bearer ' + res["access_token"]}
        )

    valid_job_body = json.load(open("valid-job-body.json"))
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
        'meal_plan_person': config['meal_plan_person'],
        'valid_job_body': valid_job_body
    }
    return config_data


def get_person_by_ids(params):
    global api_url
    return session.get(api_url, params=params)


def get_person_by_osu_id(osu_id):
    global api_url
    return session.get(api_url + osu_id)


def get_jobs_by_osu_id(osu_id):
    global api_url
    return session.get(api_url + osu_id + '/jobs')


def get_image_by_osu_id(osu_id, params=None):
    global api_url
    return session.get(api_url + osu_id + '/image', params=params)


def get_meal_plans_by_osu_id(osu_id):
    global api_url
    return session.get(api_url + osu_id + '/meal-plans')


def get_meal_plan_by_id(osu_id, meal_plan_id):
    global api_url
    return session.get(api_url + osu_id + '/meal-plans/' + meal_plan_id)


def post_job_by_osu_id(osu_id, body):
    global api_url
    return session.post(api_url + osu_id + '/jobs',
                        headers={"Content-Type": "application/json"},
                        data=json.dumps(body))
