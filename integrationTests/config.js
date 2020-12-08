let cesFqdn = process.env.CES_FQDN;
if (!cesFqdn) {
  // url from ecosystem with private network
  cesFqdn = "192.168.42.2"
}

let webdriverType = process.env.WEBDRIVER;
if (!webdriverType) {
  webdriverType = 'local';
}

let enableVideoRecording = false;
if(process.env.ENABLE_VIDEO_RECORDING) {
    console.log('...video recording will be enabled during test execution')
    enableVideoRecording = true;
}

module.exports = {
    fqdn: cesFqdn,
    baseUrl: 'https://' + cesFqdn,
    jenkinsContextPath: '/jenkins',
    username: 'ces-admin',
    password: 'ecosystem2016',
    firstname: 'admin',
    lastname: 'admin',
    displayName: 'admin',
    email: 'ces-admin@cloudogu.com',
    webdriverType: webdriverType,
    debug: true,
    adminGroup: 'CesAdministrators',
    enableVideoRecording: enableVideoRecording
};
