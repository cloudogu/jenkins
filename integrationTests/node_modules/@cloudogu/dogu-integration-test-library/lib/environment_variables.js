//######################################################################################################################

/**
 * Defines the name of the environment variable that saves the currently configured dogu name.
 * @type {string}
 */
const Env_DoguName = "DoguName"
module.exports.Env_DoguName = Env_DoguName

/**
 * Defines the default name of the dogu. Can be overriden in the cypress.json.
 * @type {string}
 */
module.exports.Default_DoguName = "redmine"

/**
 * Returns the currently configured name for the dogu under test.
 * @returns {String}
 */
module.exports.GetDoguName = function () {
    return Cypress.env(Env_DoguName)
}

//######################################################################################################################

/**
 * Defines the name of the environment variable that saves the currently configured number of login attempts.
 * @type {string}
 */
const Env_MaxLoginRetries = "MaxLoginRetries"
module.exports.Env_MaxLoginRetries = Env_MaxLoginRetries


/**
 * Defines the default number of login attempts before failing the test. Can be overriden in the cypress.json.
 * @type {number}
 */
module.exports.Default_MaxLoginRetries = 3

/**
 * Returns the currently configured number of login attempts before failing a test.
 * @returns {number}
 */
module.exports.GetMaxRetryCount = function () {
    return Cypress.env(Env_MaxLoginRetries)
}

//######################################################################################################################

/**
 * Defines the name of the environment variable that saves the currently configured admin username.
 * @type {string}
 */
const Env_AdminUsername = "AdminUsername"
module.exports.Env_AdminUsername = Env_AdminUsername

/**
 * Defines the default username of the ces administrator. Can be overriden in the cypress.json.
 * @type {string}
 */
module.exports.Default_AdminUsername = "ces-admin"

/**
 * Returns the currently configured username for the ces admin.
 * @returns {String}
 */
module.exports.GetAdminUsername = function () {
    return Cypress.env(Env_AdminUsername)
}


//######################################################################################################################

/**
 * Defines the name of the environment variable that saves the currently configured admin password.
 * @type {string}
 */
const Env_AdminPassword = "AdminPassword"
module.exports.Env_AdminPassword = Env_AdminPassword

/**
 * Defines the default password of the ces administrator. Can be overriden in the cypress.json.
 * @type {string}
 */
module.exports.Default_AdminPassword = "ecosystem2016"

/**
 * Returns the currently configured password for the ces admin.
 * @returns {String}
 */
module.exports.GetAdminPassword = function () {
    return Cypress.env(Env_AdminPassword)
}

//######################################################################################################################

/**
 * Defines the name of the environment variable that saves the currently configured admin group.
 * @type {string}
 */
const Env_AdminGroup = "AdminGroup"
module.exports.Env_AdminGroup = Env_AdminGroup

/**
 * Defines the default admin group of the ces administrator. Can be overriden in the cypress.json.
 * @type {string}
 */
module.exports.Default_AdminGroup = "CesAdministrators"

/**
 * Returns the currently configured user group that have administrative privileges in the ces.
 * @returns {String}
 */
module.exports.GetAdminGroup = function () {
    return Cypress.env(Env_AdminGroup)
}

//######################################################################################################################



