const {
    Then
} = require("@badeball/cypress-cucumber-preprocessor");

Then(/^the user has administrator privileges in the dogu gui$/, function () {
    cy.isJenkinsAdminGui();
});

Then(/^the user has no administrator privileges in the dogu gui$/, function () {
    cy.isNotJenkinsAdminGui();
});
