const fakeOsuId = '999999999';
const fakeBaseUrl = '/v2';
const rawPerson = {
  displayFirstName: 'Lord',
  displayMiddleName: null,
  displayLastName: 'Voldemort',
  osuId: fakeOsuId,
  firstName: 'Tom',
  middleName: 'Marvolo',
  lastName: 'Riddle',
  birthDate: '1926-12-31',
  confidentialInd: 'N',
  citizenCode: 'C',
  citizenDescription: 'Citizen',
  sex: 'M',
  onid: 'riddlet',
  currentStudentInd: 'N',
  employeeStatusCode: 'A',
  ssnStatus: 'valid',
  lastPaidDate: '2020-05-31',
};

const rawEmail = {
  emailId: '99999',
  'emailType.code': 'EMPL',
  'emailType.description': 'Employee',
  emailAddress: 'AAAaOpAAPAANlbzABa@nobody.nobody',
  comment: null,
  preferredInd: 'N',
  lastActivityDate: '2020-03-27',
};

export {
  fakeOsuId,
  fakeBaseUrl,
  rawPerson,
  rawEmail,
};
