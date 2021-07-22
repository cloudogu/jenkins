const {
    When,
    Then
} = require("cypress-cucumber-preprocessor/steps");

let key;

When(/^the user generates an apikey$/, function () {
    cy.createJenkinsApiKey().then((apikey) => {
        key = apikey;
    })
});

Then(/^the user can use his apikey$/, function () {
    cy.logout();
    cy.authWithApiKey(key).then((responseCode) => {
        expect(responseCode === 200).to.be.true;
    });
});
