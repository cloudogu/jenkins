const {
    When,
    Then
} = require("@badeball/cypress-cucumber-preprocessor");

When(/^the user navigates to tool configuration admin page$/, function () {
    cy.navigateToToolConfigAdminPage();
});

Then(/^the user sees the maven m3 installation$/, function () {
    cy.searchM3Installation();
});

