let cesFqdn = process.env.CES_FQDN;
if (!cesFqdn) {
  // url from ecosystem with private network
  cesFqdn = "192.168.56.2"
}

let webdriverType = process.env.WEBDRIVER;
if (!webdriverType) {
  webdriverType = 'local';
}

module.exports = {
    fqdn: cesFqdn,
    baseUrl: 'https://' + cesFqdn,
    jenkinsContextPath: '/jenkins',
    username: 'admin',
    password: 'admin',
    firstname: 'admin',
    lastname: 'admin',
    displayName: 'admin',
    email: 'cwolfes@triology.de',
    webdriverType: webdriverType,
    debug: true,
    adminGroup: 'admin'
};
