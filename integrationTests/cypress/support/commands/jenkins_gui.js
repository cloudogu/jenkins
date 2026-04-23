const jenkinsLogout = () => {
  cy.get('a#root-action-UserAction')
    .should('be.visible')
    .trigger('mouseenter');

  cy.get('div[id^="tippy-"]').invoke('attr', 'style', 'visibility: visible; opacity: 1; display: block;');

  cy.contains('Sign out').click({ force: true });

  cy.url().should('include', '/cas/logout');
};

const navigateToToolConfigAdminPage = () => {
    cy.visit("/jenkins/configureTools");
};

const searchM3Installation = () => {
    cy.get("button").contains("Maven installations").click();
    cy.get("input[value='M3']");
};

const createJenkinsApiKey = () => {
    return cy.fixture("testuser_data").then((user) => {
        cy.visit("/jenkins/user/" + user.username + "/security");

        cy.get("button.api-token-property-add:visible")
            .first()
            .click();

        cy.get('dialog input[name="tokenName"]')
            .should("be.visible")
            .type("e2e-token");

        cy.get('dialog button[data-id="ok"]')
            .should("be.visible")
            .and("not.be.disabled")
            .click();

        return cy.get("span.api-token-new-value", { timeout: 10000 })
            .should("be.visible")
            .invoke("text")
            .should("not.be.empty");
    });
};

const getUserAttributesGui = (testUser) => {
    cy.visit(Cypress.config().baseUrl + "/jenkins/user/" + testUser.username + "/account");
    cy.get("input[name='email.address']").invoke("val").then((mail) => {
        cy.get("input[name='_.fullName']").invoke("val").then((fullName) => {
            return {
                mail: mail,
                fullName: fullName
            };
        })
    });
};

const isJenkinsAdminGui = () => {
    cy.get("a[href='/jenkins/manage']");
}

const isNotJenkinsAdminGui = () => {
    cy.get("a[href='/jenkins/manage']").should("not.exist");
}

Cypress.Commands.add("jenkinsLogout", jenkinsLogout);
Cypress.Commands.add("navigateToToolConfigAdminPage", navigateToToolConfigAdminPage);
Cypress.Commands.add("searchM3Installation", searchM3Installation);
Cypress.Commands.add("createJenkinsApiKey", createJenkinsApiKey);
Cypress.Commands.add("getUserAttributesGui", getUserAttributesGui);
Cypress.Commands.add("isJenkinsAdminGui", isJenkinsAdminGui);
Cypress.Commands.add("isNotJenkinsAdminGui", isNotJenkinsAdminGui);
