const config = require('./config');
const utils = require('./utils');
const AdminFunctions = require('./adminFunctions');

const webdriver = require('selenium-webdriver');
const By = webdriver.By;
const until = webdriver.until;

jest.setTimeout(30000);

process.env.NODE_TLS_REJECT_UNAUTHORIZED = '0';


let driver;
let adminFunctions;

beforeEach(async() => {
    driver = utils.createDriver(webdriver);
    adminFunctions = new AdminFunctions('testUser', 'testUser', 'testUser', 'testUser@test.de', 'testuserpassword');
    await adminFunctions.createUser();
});

afterEach(async() => {
    await driver.findElement(By.xpath("//div[@id='header']/div[2]/span/a[2]/b")).click();
    await adminFunctions.removeUser(driver);
    await driver.quit();
});


describe('user permissions', () => {

    test('user (testUser) has admin privileges', async() => {
        driver.get(utils.getCasUrl(driver));
        adminFunctions.giveAdminRights();
        adminFunctions.testUserLogin(driver);
        driver.wait(until.elementLocated(By.className('login')), 5000);
        var adminPermissions = await utils.isAdministrator(driver);
        expect(adminPermissions).toBe(true);
    });

    test('user (testUser) has no admin privileges', async() => {
        driver.get(utils.getCasUrl(driver));
        adminFunctions.testUserLogin(driver);
        driver.wait(until.elementLocated(By.className('login')), 5000);
        var adminPermissions = await utils.isAdministrator(driver);
        expect(adminPermissions).toBe(false);
    });

    test('user (testUser) remove admin privileges', async() => {
        await driver.get(utils.getCasUrl(driver));
        await adminFunctions.giveAdminRights();
        await adminFunctions.testUserLogin(driver);
        await driver.wait(until.elementLocated(By.className('login')), 5000);
        await adminFunctions.testUserLogout(driver);
        await driver.wait(until.elementLocated(By.className('success')), 5000);
        await adminFunctions.takeAdminRights();
        await driver.get(utils.getCasUrl(driver));
        await adminFunctions.testUserLogin(driver);
        await driver.wait(until.elementLocated(By.className('login')), 5000);
        var adminPermissions = await utils.isAdministrator(driver);
        expect(adminPermissions).toBe(false);
    });

});