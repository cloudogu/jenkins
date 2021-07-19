const {
    After,
} = require("cypress-cucumber-preprocessor/steps");
const env = require('../environment_variables.js')

module.exports.register = function () {
    /**
     * Deletes the created testuser after every scenario
     */
    After({tags: "@requires_testuser"}, () => {
        cy.logout();

        cy.fixture("testuser_data").then(function (testUser) {
            cy.log("Removing test user")
            cy.usermgtDeleteUser(testUser.username)
            cy.deleteUserFromDoguViaAPI(testUser.username, false)
        })
    });
}