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

const fakeMealPlanId = '1';
const rawMealPlan = {
  mealPlanId: fakeMealPlanId,
  mealPlan: '!OrangeCash',
  balance: '9000.1',
  'lastUsed.dateTime': '2019-03-21T10:53:06Z',
  'lastUsed.location': 'North_Porch_Cafe',
};
const serializedMealPlan = {
  links: {
    self: `https://localhost/v2/persons/${fakeOsuId}/meal-plans/${fakeMealPlanId}`,
  },
  data: {
    type: 'meal-plans',
    id: fakeMealPlanId,
    links: {
      self: `https://localhost/v2/persons/${fakeOsuId}/meal-plans/${fakeMealPlanId}`,
    },
    attributes: {
      mealPlan: '!OrangeCash',
      balance: '9000.1',
      lastUsed: {
        dateTime: '2019-03-21T10:53:06Z',
        location: 'North_Porch_Cafe',
      },
    },
  },
};
const serializedMealPlans = {
  links: {
    self: `https://localhost/v2/persons/${fakeOsuId}/meal-plans`,
  },
  data: [
    {
      type: 'meal-plans',
      id: fakeMealPlanId,
      links: {
        self: `https://localhost/v2/persons/${fakeOsuId}/meal-plans/${fakeMealPlanId}`,
      },
      attributes: {
        mealPlan: '!OrangeCash',
        balance: '9000.1',
        lastUsed: {
          dateTime: '2019-03-21T10:53:06Z',
          location: 'North_Porch_Cafe',
        },
      },
    },
    {
      type: 'meal-plans',
      id: fakeMealPlanId,
      links: {
        self: `https://localhost/v2/persons/${fakeOsuId}/meal-plans/${fakeMealPlanId}`,
      },
      attributes: {
        mealPlan: '!OrangeCash',
        balance: '9000.1',
        lastUsed: {
          dateTime: '2019-03-21T10:53:06Z',
          location: 'North_Porch_Cafe',
        },
      },
    },
  ],
};

export {
  fakeOsuId,
  fakeBaseUrl,
  rawPerson,
  fakeMealPlanId,
  rawMealPlan,
  serializedMealPlan,
  serializedMealPlans,
};
