
const request = require('supertest');
const config = require('./config');
const AdminFunctions = require('./adminFunctions');
const utils = require('./utils');
const webdriver = require('selenium-webdriver');
const By = webdriver.By;
const keys = webdriver.Key;
const until = webdriver.until;

jest.setTimeout(30000);
let driver;
let adminFunctions;

// disable certificate validation
process.env.NODE_TLS_REJECT_UNAUTHORIZED = '0';

beforeEach(async() => {
    driver = utils.createDriver(webdriver);
    adminFunctions = new AdminFunctions('testUserR', 'testUserR', 'testUserR', 'testUserR@test.de', 'testuserrpasswort');
    await adminFunctions.createUser();
});

afterEach(async() => {

    await adminFunctions.removeUser(driver);
    await driver.quit();
});


describe('administration rest tests', () => {

    test('rest - user (testUser) has admin privileges', async() => {
        await adminFunctions.giveAdminRights();
        await adminFunctions.accessUsersJson(200);
    });

    test('rest - user (testUser) has no admin privileges', async() => {
        await driver.get(utils.getCasUrl(driver));
        await adminFunctions.accessUsersJson(403);
    });


    test('rest - user (testUser) remove admin privileges', async() => {

        await driver.get(utils.getCasUrl(driver));
        adminFunctions.testUserLogin(driver); // test user login to update information in redmine
        await adminFunctions.testUserLogout(driver);
        adminFunctions.takeAdminRights();
        await driver.get(utils.getCasUrl(driver));
        await adminFunctions.accessUsersJson(403);
    });

});