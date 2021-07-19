const {
    Before,
} = require("cypress-cucumber-preprocessor/steps");
const env = require('../environment_variables.js')

module.exports.register = function () {
    /**
     * Create a testuser which has no admin rights to perform user operations
     */
    Before({tags: "@requires_testuser"}, () => {
        cy.fixture("testuser_data").then(function (testUser) {
            cy.usermgtTryDeleteUser(testUser.username)
            cy.deleteUserFromDoguViaAPI(testUser.username, false)
            cy.log("Creating test user")
            cy.usermgtCreateUser(testUser.username, testUser.givenname, testUser.surname, testUser.displayName, testUser.mail, testUser.password)
        })
    });
}