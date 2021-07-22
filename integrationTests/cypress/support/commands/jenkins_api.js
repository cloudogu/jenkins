const jenkinsIsAdmin = () => {
    cy.fixture("testuser_data").then((testUser) => {
        cy.request({
            method: "GET",
            url: Cypress.config().baseUrl + "/jenkins/pluginManager/api/json",
            auth: {
                username: testUser.username,
                password: testUser.password
            },
            failOnStatusCode: false
        }).then((response) => {
            return response.status;
        })
    })
}

Cypress.Commands.add("jenkinsIsAdmin", jenkinsIsAdmin);
