const {
    When,
    Then
} = require("@badeball/cypress-cucumber-preprocessor");

// Loads all steps from the dogu integration library into this project
const doguTestLibrary = require('@cloudogu/dogu-integration-test-library')
doguTestLibrary.registerSteps();

//Implement all necessary steps fore dogu integration test library
When(/^the user clicks the dogu logout button$/, function () {
    cy.jenkinsLogout();
});

Then(/^the user has no administrator privileges in the dogu$/, function () {
    cy.jenkinsIsAdmin().then((statusCode) => {
        expect(statusCode === 403).to.be.true;
    });
});

Then(/^the user has administrator privileges in the dogu$/, function () {
    cy.jenkinsIsAdmin().then((statusCode) => {
        expect(statusCode === 200).to.be.true;
    });
});