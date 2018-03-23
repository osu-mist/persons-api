FROM python:3

WORKDIR /usr/src/app

COPY requirements.txt ./
RUN pip3 install -r requirements.txt

COPY . .
USER nobody:nogroup
ENTRYPOINT ["python3"]
CMD ["integrationtests.py", "-v", "-i", "configuration.json"]
