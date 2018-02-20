const config = require('./config');
const utils = require('./utils');

const webdriver = require('selenium-webdriver');
const By = webdriver.By;

jest.setTimeout(30000);

let driver;

beforeEach(() => {
    driver = utils.createDriver(webdriver);
});

afterEach(() => {
    driver.quit();
});

describe('cas login', () => {
    test('redirect to cas', async () => {
        driver.get(config.baseUrl + config.jenkinsContextPath);
        const url = await driver.getCurrentUrl();
        expect(url).toMatch('/cas/login');
    });

    test('cas authentication', async() => {
        driver.get(utils.getCasUrl(driver));
        utils.login(driver);
        const username = await driver.findElement(By.className('login')).getText();
        expect(username).toContain(config.username);
    });

    test('cas logout', async() => {
        driver.get(utils.getCasUrl(driver));
        utils.login(driver);
        await driver.findElement(By.xpath("//div[@id='header']/div[2]/span/a[2]/b")).click();
        const url = await driver.getCurrentUrl();
        expect(url).toMatch("/cas/logout");
    });


});