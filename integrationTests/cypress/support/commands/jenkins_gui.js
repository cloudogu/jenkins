const env = require('@cloudogu/dogu-integration-test-library/lib/environment_variables')


const jenkinsLogout = () => {
    cy.get("[a href='jenkins/logout']").click();
}

const jenkinsIsAdmin = () => {
    return cy.get("[a href='/jenkins/manage']") !== null;
}

const jenkinsDeleteUser = (user) => {
    cy.wait(2000);
    cy.visit(Cypress.config().baseUrl + "/jenkins/user/" + user + "/delete");
    cy.wait(2000);
    cy.get("id='yui-gen1-button'").click();
}

Cypress.Commands.add("jenkinsLogout", jenkinsLogout);
Cypress.Commands.add("jenkinsIsAdmin", jenkinsIsAdmin);
Cypress.Commands.add("jenkinsDeleteUser", jenkinsDeleteUser);