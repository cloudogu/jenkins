const config = require('./config');
const utils = require('./utils');
const AdminFunctions = require('./adminFunctions');

const webdriver = require('selenium-webdriver');
const By = webdriver.By;
const until = webdriver.until;

jest.setTimeout(60000);

process.env.NODE_TLS_REJECT_UNAUTHORIZED = '0';


let driver;
let adminFunctions;

beforeEach(async() => {
    driver = await utils.createDriver(webdriver);
    await driver.manage().window().maximize();
    adminFunctions = new AdminFunctions('testUser', 'testUser', 'testUser', 'testUser@test.de', 'testuserpassword');
    await adminFunctions.createUser();
});

afterEach(async() => {
    await driver.get(config.baseUrl + config.jenkinsContextPath + "/logout");
    await adminFunctions.removeUser(driver);
    await driver.quit();
});


describe('user permissions', () => {
    test('init090NORMmavenautoinstall: M3 maven installer has been created', async() => {
        await driver.get(utils.getCasUrl(driver));
        await adminFunctions.giveAdminRights();
        await adminFunctions.testUserLogin(driver);
        await driver.wait(until.elementLocated(By.className('login')), 5000);

        // go to tool configuration admin page
        await driver.get(config.baseUrl + config.jenkinsContextPath + "/configureTools");
        await driver.executeScript("window.scrollBy(0,1000)")
        // click on maven installations button
        await driver.wait(until.elementLocated(By.xpath("//button[contains(text(), 'Maven installations')]")),3000);
        await driver.findElement(By.xpath("//button[contains(text(), 'Maven installations')]")).click();
        // get all elements with "M3"
        const elementsWithM3 = await driver.findElements(By.css("input[value='M3']"))
        // there should be one occurence
        expect(elementsWithM3.length == 1).toBe(true);
    });
});