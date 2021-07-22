const jenkinsLogout = () => {
    cy.get("a[href='/jenkins/logout']").click();
}

const navigateToToolConfigAdminPage = () => {
    cy.visit("/jenkins/configureTools");
}

const searchM3Installation = () => {
    cy.get("button").contains("Maven installations").click();
    cy.get("input[value='M3']");
}

const createJenkinsApiKey = () => {
    cy.fixture("testuser_data").then((user) => {
        cy.visit("/jenkins/user/" + user.username + "/configure")
    })
    cy.get("button").contains("Add new Token").click();
    cy.get("button").contains("Generate").click();
    return cy.get("span[class='new-token-value visible']").invoke("text");
}

Cypress.Commands.add("jenkinsLogout", jenkinsLogout);
Cypress.Commands.add("navigateToToolConfigAdminPage", navigateToToolConfigAdminPage);
Cypress.Commands.add("searchM3Installation", searchM3Installation);
Cypress.Commands.add("createJenkinsApiKey", createJenkinsApiKey);