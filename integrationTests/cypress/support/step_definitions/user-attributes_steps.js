const {
    Then
} = require("cypress-cucumber-preprocessor/steps");

let key;

Then(/^the user can view his attributes with profile page$/, function () {
    cy.fixture("testuser_data").then((testUser) => {
        cy.getUserAttributesGui(testUser).then((guiResponse) => {
            expect(guiResponse.fullName === testUser.displayName).to.be.true;
            expect(guiResponse.mail === testUser.mail).to.be.true;
        });
    })

});

Then(/^the user can view his attributes with rest$/, function () {
    cy.createJenkinsApiKey().then((apikey) => {
        key = apikey;
    })
    cy.logout();
    cy.fixture("testuser_data").then((testUser) => {cy.getUserAttributesApi(key, testUser).then((response) => {
        expect(response.status === 200).to.be.true;
        expect(response.body.fullName === testUser.displayName).to.be.true;
        expect(response.body.property[response.body.property.length-1].address === testUser.mail).to.be.true;
    })});
});


