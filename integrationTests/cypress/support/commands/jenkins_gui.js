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

Cypress.Commands.add("jenkinsLogout", jenkinsLogout);
Cypress.Commands.add("navigateToToolConfigAdminPage", navigateToToolConfigAdminPage);
Cypress.Commands.add("searchM3Installation", searchM3Installation);