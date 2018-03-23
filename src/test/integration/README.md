# Persons API Integration Tests

The integration tests for Persons API.

## Configuration

1. Register an application via [OSU Developer Portal](https://developer.oregonstate.edu/)
2. Get `client_id` and `client_secret` from your app, then copy
[configuration-example.json](./configuration-example.json) as `configuration.json` and modify it as necessary.

## Usage

1. To install app dependencies, simply run:

```
$ pip install -r requirements.txt
```

2. Run the integration tests:

```
$ python integrationtests.py -v -i configuration.json
```

## Docker

```shell
$ docker build -t persons-api-tests .
# Run the integration tests in Unix
$ docker run -v "$PWD"/configuration.json:/usr/src/app/configuration.json:ro persons-api-tests
# Run the integration tests in Windows
$ docker run -v c:\path\to\configuration.json:/c:\usr\src\app\configuration.json:ro persons-api-tests
```
