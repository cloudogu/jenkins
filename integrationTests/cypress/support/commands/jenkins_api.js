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

const authWithApiKey = (apiKey) => {
    cy.fixture("testuser_data").then((testUser) => {
        cy.request({
            method: "GET",
            url: Cypress.config().baseUrl + "/jenkins/api/json",
            auth : {
                username: testUser.username,
                password: apiKey
            },
            failOnStatusCode: false
        }).then((response) => {
            return response.status;
        })
    })
}

const getUserAttributesApi = (apiKey, testUser) => {
    const username = testUser.username;
    cy.request({
        method: "GET",
        url: Cypress.config().baseUrl + "/jenkins/user/" + username + "/api/json",
        auth : {
            username: username,
            password: apiKey
        },
        failOnStatusCode: false
    }).then((response) => {
        return response;
    });
};

Cypress.Commands.add("jenkinsIsAdmin", jenkinsIsAdmin);
Cypress.Commands.add("authWithApiKey", authWithApiKey);
Cypress.Commands.add("getUserAttributesApi", getUserAttributesApi);
