const {
    Given,
} = require("cypress-cucumber-preprocessor/steps");
const env = require('../environment_variables.js')

module.exports.register = function () {
    Given(/^the user is logged into the CES$/, function () {
        cy.fixture("testuser_data").then(function (testUser) {
            cy.login(testUser.username, testUser.password)
        })
    });

    Given(/^the user is logged out of the CES$/, function () {
        cy.logout()
    });

    Given(/^the user is not member of the admin user group$/, function () {
        // default behaviour
    });

    Given(/^the user is member of the admin user group$/, function () {
        cy.fixture("testuser_data").then(function (testUser) {
            cy.promoteAccountToAdmin(testUser.username)
        })
    });

    Given(/^the user has no internal dogu account$/, function () {
        // default behaviour
    });

    Given(/^the user has an internal admin dogu account$/, function () {
        cy.fixture("testuser_data").then(function (testUser) {
            cy.isCesAdmin(testUser.username).then(function (isAdmin) {
                if (isAdmin) {
                    // create internal dogu account
                    cy.login(testUser.username, testUser.password)
                    cy.logout()
                } else {
                    // promote -> create internal dogu account -> demote
                    cy.promoteAccountToAdmin(testUser.username)
                    cy.login(testUser.username, testUser.password)
                    cy.logout()
                    cy.demoteAccountToDefault(testUser.username)
                }
            })
        })
    });

    Given(/^the user has an internal default dogu account$/, function () {
        cy.fixture("testuser_data").then(function (testUser) {
            cy.isCesAdmin(testUser.username).then(function (isAdmin) {
                if (isAdmin) {
                    // demote -> create internal dogu account -> promote
                    cy.demoteAccountToDefault(testUser.username)
                    cy.login(testUser.username, testUser.password)
                    cy.logout()
                    cy.promoteAccountToAdmin(testUser.username)
                } else {
                    // create internal dogu account
                    cy.login(testUser.username, testUser.password)
                    cy.logout()
                }
            })
        })
    });
}