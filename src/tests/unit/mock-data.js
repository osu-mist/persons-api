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
const serializedPerson = {
  links: { self: 'https://localhost/v2/persons/999999999' },
  data: {
    type: 'person',
    id: '999999999',
    links: { self: 'https://localhost/v2/persons/999999999' },
    attributes: {
      firstName: 'Tom',
      lastName: 'Riddle',
      middleName: 'Marvolo',
      birthDate: '1926-12-31',
      citizen: {
        code: 'C',
        description: 'Citizen',
      },
      sex: 'M',
      displayFirstName: 'Lord',
      displayMiddleName: null,
      displayLastName: 'Voldemort',
      onid: 'riddlet',
      confidentialInd: false,
      currentStudentInd: false,
      employeeStatus: {
        code: 'A',
        description: 'Active',
      },
      ssnStatus: 'valid',
      lastPaidDate: '2020-05-31',
    },
  },
};

const fakeMealPlanId = 'fakeMealPlanId';

export {
  fakeOsuId,
  fakeBaseUrl,
  rawPerson,
  serializedPerson,
  fakeMealPlanId,
};
