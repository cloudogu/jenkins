const env = require('@cloudogu/dogu-integration-test-library/lib/environment_variables')

let apiToken;

/**
 * Generates an Jenkins API-Token for authentication.
 * After a valid token was requested it is cached and retrieved for all subsequent calls.
 * @returns {String} A valid auth token to perform request against the portainer API.
 */
const jenkinsGetApiToken = () => {
    if (apiToken) {
        // Reuse token when available
        return apiToken
    } else {
        cy.request({
            method: "POST",
            url: Cypress.config().baseUrl + "/jenkins/me/descriptorByName/jenkins.security.ApiTokenProperty/generateNewToken",
            body: {
                'Username': env.GetAdminUsername(),
                'Password': env.GetAdminPassword()
            }
        }).then((response) => {
            expect(response.status).to.eq(200)
            if (response.body.data) {
                apiToken = response.body.data.tokenValue
                return apiToken
            } else {
                throw new Error("Valid API-Token for Jenkins expected.")
            }
        })
    }
}

/**
 * Retrieves all internal jenkins users.
 * Needs to be chained after `jenkinsGetApiToken()`
 * @param {String} apitoken - A valid auth token for the jenkins api.
 * @returns {Object} An object containing all jenkins users.
 */
const jenkinsGetUsers = (authToken) => {
    const Authorization = `Bearer ${authToken}`;
    cy.request({
        method: "GET",
        url: Cypress.config().baseUrl + "/jenkins/asynchPeople/api/json",
        headers: {
            Authorization
        }
    }).then((response) => {
        expect(response.status).to.eq(200)
        if (response.body) {
            return response.body
        } else {
            throw new Error("Valid user data expected.")
        }
    })
}

/**
 * Retrieves internal jenkins user.
 * Needs to be chained after `jenkinsGetApiToken()`
 * @param {String} apitoken - A valid auth token for the jenkins api.
 * @param {String} username - Username
 * @returns {Object} An object containing jenkins user.
 */
const jenkinsGetUser = (authToken, username) => {
    const Authorization = `Bearer ${authToken}`;
    cy.request({
        method: "GET",
        url: Cypress.config().baseUrl + "/jenkins/user/" + username + "/api/json",
        headers: {
            Authorization
        }
    }).then((response) => {
        expect(response.status).to.eq(200)
        if (response.body) {
            return response.body
        } else {
            throw new Error("Valid user data expected.")
        }
    })
}

module.exports.register = function () {
    Cypress.Commands.add("jenkinsApiToken", jenkinsGetApiToken);
    Cypress.Commands.add("jenkinsGetUsers", jenkinsGetUsers);
    Cypress.Commands.add("jenkinsGetUser", jenkinsGetUser);
}