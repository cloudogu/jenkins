const {
    When,
    Then
} = require("cypress-cucumber-preprocessor/steps");

When(/^the user navigates to tool configuration admin page$/, function () {
    cy.navigateToToolConfigAdminPage();
});

Then(/^the user sees the maven m3 installation$/, function () {
    cy.searchM3Installation();
});

