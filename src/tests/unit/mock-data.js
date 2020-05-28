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

const rawAddress = {
  addressId: 'cf4c689f-94d4-42df-af59-9934cccdc52e',
  'addressType.code': 'PA',
  'addressType.description': 'Student Alternate Contact',
  addressLine1: '188 W Jadon Dr',
  addressLine2: null,
  addressLine3: null,
  addressLine4: null,
  houseNumber: null,
  city: 'Lebanon',
  stateCode: 'OR',
  state: 'Oregon',
  postalCode: '97355-1686',
  countyCode: '41043',
  county: 'Linn',
  nationCode: null,
  nation: null,
  lastModified: '2015-10-19',
};

const fakePhoneBody = {
  addressType: {},
  phoneType: {},
};

export {
  fakeOsuId,
  fakeBaseUrl,
  rawPerson,
  rawAddress,
  fakePhoneBody,
};
