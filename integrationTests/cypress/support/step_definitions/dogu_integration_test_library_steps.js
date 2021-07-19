const {
    When,
    Then
} = require("cypress-cucumber-preprocessor/steps");
const env = require('@cloudogu/dogu-integration-test-library/lib/environment_variables')

// Loads all steps from the dogu integration library into this project
const doguTestLibrary = require('@cloudogu/dogu-integration-test-library')
doguTestLibrary.registerSteps();

//Implement all necessary steps fore dogu integration test library
When(/^the user clicks the dogu logout button$/, function () {
    cy.wait(4000);
    cy.jenkinsLogout();
});

Then(/^the user has no administrator privileges in the dogu$/, function () {
    cy.wait(2000);
    expect(cy.jenkinsIsAdmin()).to.be.false;
});

Then(/^the user has administrator privileges in the dogu$/, function () {
    cy.wait(2000);
    expect(cy.jenkinsIsAdmin()).to.be.true;
});