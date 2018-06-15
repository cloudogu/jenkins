const config = require('./config');

const webdriver = require('selenium-webdriver');
const By = webdriver.By;
const until = webdriver.until;

const chromeCapabilities = webdriver.Capabilities.chrome();

const chromeOptions = {
    'args': ['--test-type', '--start-maximized']
};
chromeCapabilities.set('chromeOptions', chromeOptions);
chromeCapabilities.set('name', 'Jenkins ITs');

exports.createDriver = function(){
    if (config.webdriverType === 'local') {
        return createLocalDriver();
    }
    return createRemoteDriver();
};

function createRemoteDriver() {
    return new webdriver.Builder().withCapabilities(chromeCapabilities)
    .build();
}

function createLocalDriver() {
  return new webdriver.Builder()
    .withCapabilities(chromeCapabilities)
    .usingServer('http://localhost:4444/wd/hub')
    .build();
}



exports.getCasUrl = async function getCasUrl(driver){
    await driver.get(config.baseUrl + config.jenkinsContextPath);
    return driver.getCurrentUrl();
};

exports.login = async function login(driver) {
    await driver.wait(until.elementLocated(By.id('password')), 5000);
    await driver.wait(until.elementLocated(By.id('username')), 5000);

    await driver.findElement(By.id('username')).sendKeys(config.username);
    await driver.findElement(By.id('password')).sendKeys(config.password);
    return driver.findElement(By.css('input[name="submit"]')).click();
};

exports.isAdministrator = async function isAdministrator(driver){
    return await driver.findElement(By.xpath("(//a[@href='/jenkins/manage'])")).then(function() {
        return true;//element was found
    }, function(err) {
        if (err instanceof webdriver.error.NoSuchElementError) {
            return false;//element did not exist
        } else {
            webdriver.promise.rejected(err);
        }
    });
};
