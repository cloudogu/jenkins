const config = require('./config');
const utils = require('./utils');
const request = require('supertest');

const webdriver = require('selenium-webdriver');
const By = webdriver.By;

jest.setTimeout(30000);

process.env.NODE_TLS_REJECT_UNAUTHORIZED = '0';


let driver;

beforeEach(() => {
    driver = utils.createDriver(webdriver);
});

afterEach(() => {
    driver.quit();
});


describe('cas rest basic authentication', () => {

    test('authentication with username password', async () => {
        await request(config.baseUrl)
            .get(config.jenkinsContextPath + "/api/json")
            .auth(config.username, config.password)
            .expect(200);
    });

    /*login -> click on username -> configure -> show api token*/
    test('authentication with API key', async () => {
        driver.get(utils.getCasUrl(driver));
        utils.login(driver);
        await driver.findElement(By.xpath("//div[@id='header']/div[2]/span/a/b")).click();
        await driver.findElement(By.linkText("Configure")).click();
        await driver.findElement(By.id("yui-gen1-button")).click();
        const input = await driver.findElement(By.id("apiToken"));
        const apikey = await input.getAttribute("value");
        await request(config.baseUrl)
            .get(config.jenkinsContextPath+"/api/json")
            .auth(config.username, apikey)
            .expect(200);
    });


});


