const config = require('./config');

const webdriver = require('selenium-webdriver');
const By = webdriver.By;

exports.createDriver = function(){
    if (config.webdriverType === 'local') {
        return createLocalDriver();
    }
    return createRemoteDriver();
};

function createRemoteDriver() {
    return new webdriver.Builder()
    .build();
}

function createLocalDriver() {
  return new webdriver.Builder()
    .withCapabilities(webdriver.Capabilities.chrome())
    .build();
}

exports.getCasUrl = async function getCasUrl(driver){
    driver.get(config.baseUrl + config.jenkinsContextPath);
    return await driver.getCurrentUrl();
};

exports.login = async function login(driver) {
    driver.findElement(By.id('username')).sendKeys("admin");
    driver.findElement(By.id('password')).sendKeys("admin");
    return await driver.findElement(By.css('input[name="submit"]')).click();
};
