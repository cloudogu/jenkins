const config = require('./config');
const utils = require('./utils');
const request = require('supertest');

const webdriver = require('selenium-webdriver');
const By = webdriver.By;
const until = webdriver.until;
jest.setTimeout(30000);

process.env.NODE_TLS_REJECT_UNAUTHORIZED = '0';


let driver;

beforeEach(async () => {
    driver = utils.createDriver(webdriver);
    // await driver.manage().window().maximize();
});

afterEach(async () => {
    await driver.quit();
});


describe('cas rest basic authentication', () => {

    test('authentication with username password', async () => {
        await request(config.baseUrl)
            .get(config.jenkinsContextPath + "/api/json")
            .auth(config.username, config.password)
            .expect(200);
    });

    test('authentication with API key', async () => {
        await driver.get(utils.getCasUrl(driver));
        await utils.login(driver);
        // go to user configuration page
        await driver.get(config.baseUrl + config.jenkinsContextPath + "/user/" + config.username + "/configure");
        await driver.wait(until.elementLocated(By.id('yui-gen2-button')), 5000);
        // click "Add new Token" button
        await driver.findElement(By.id('yui-gen2-button')).click();
        await driver.wait(until.elementLocated(By.className('setting-input   token-name')), 5000);
        // create new token with random name
        let newBackupTimeInputField = await driver.findElement(By.className("setting-input   token-name"));
        await newBackupTimeInputField.clear();
        let randomName = "1234"; //Math.random().toString(36);
        await newBackupTimeInputField.sendKeys(randomName);
        await driver.findElement(By.className("yui-button token-save")).click();
        // await driver.findElement(By.id("yui-gen1-button")).click();
        // get new generated token
        const input = await driver.findElement(By.className('new-token-value visible'));
        const apikey = await input.getText();
        await request(config.baseUrl)
            .get(config.jenkinsContextPath+"/api/json")
            .auth(config.username, apikey)
            .expect(200);
    });


});


describe('rest attributes', () => {

    test('rest - user attributes', async () => {
        const response = await request(config.baseUrl)
            .get(config.jenkinsContextPath + '/user/' + config.username + '/api/json')
            .auth(config.username, config.password)
            .expect('Content-Type', /json/)
            .expect(200);

        expect(response.body.fullName).toBe(config.displayName);
        expect(response.body.property[response.body.property.length-1].address).toBe(config.email);

    });

    test('rest - user is administrator', async () => {
        await request(config.baseUrl)
            .get(config.jenkinsContextPath+"/pluginManager/api/json")
            .auth(config.username, config.password)
            .expect(200);
    });



});
