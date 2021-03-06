const config = require('./config');
const utils = require('./utils');
const webdriver = require('selenium-webdriver');
const By = webdriver.By;

const logoutUrl = '/cas/logout';
const loginUrl = '/cas/login';


jest.setTimeout(30000);

let driver;

beforeEach(async() => {
    driver = utils.createDriver(webdriver);
    await driver.manage().window().maximize();
});

afterEach(async () => {
    await driver.quit();
});

describe('cas browser login', () => {

    test('automatic redirect to cas login', async () => {
        await driver.get(config.baseUrl + config.jenkinsContextPath);
        const url = await driver.getCurrentUrl();
        expect(url).toMatch(loginUrl);
    });

    test('login', async() => {
        await driver.get(utils.getCasUrl(driver));
        await utils.login(driver);
        const username = await driver.findElement(By.className('login')).getText();
        expect(username.toLowerCase()).toContain(config.displayName);
    });

    test('logout front channel', async() => {
        await driver.get(utils.getCasUrl(driver));
        await utils.login(driver);
        await driver.findElement(By.xpath("/html/body/div[2]/header/div[3]/a[2]/span")).click();
        const url = await driver.getCurrentUrl();
        expect(url).toMatch(logoutUrl);
    });

    test('logout back channel', async() => {
        await driver.get(utils.getCasUrl(driver));
        await utils.login(driver);
        await driver.get(config.baseUrl + logoutUrl);
        await driver.get(config.baseUrl + config.jenkinsContextPath);
        const url = await driver.getCurrentUrl();
        expect(url).toMatch(loginUrl);
    });

});


describe('browser attributes', () => {

    test('front channel user attributes', async () => {
        await driver.get(utils.getCasUrl(driver));
        await utils.login(driver);
        await driver.get(config.baseUrl + config.jenkinsContextPath + "/user/" + config.username + "/configure");
        const emailAddressInput = await driver.findElement(By.name("email.address"));
        const emailAddress = await emailAddressInput.getAttribute("value");
        const usernameInput= await driver.findElement(By.name('_.fullName'));
        const username = await usernameInput.getAttribute("value");
        expect(username).toBe(config.displayName);
        expect(emailAddress).toBe(config.email);
    });

    test('front channel user administrator', async () => {
        await driver.get(utils.getCasUrl(driver));
        await utils.login(driver);
        const isAdministrator = await utils.isAdministrator(driver);
        expect(isAdministrator).toBe(true);
    });



});




