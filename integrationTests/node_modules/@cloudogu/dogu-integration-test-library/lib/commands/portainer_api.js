const env = require('../environment_variables.js')
// ***********************************************
// REST - /api/auth
// ***********************************************

// The auth token required by the portainer api. Can be generated & retrieved by `portainerGetAuthToken`
let authToken;

/**
 * Generates an Portainer JWT Token for authentication.
 * After a valid token was requested it is cached and retrieved for all subsequent calls.
 * @returns {String} A valid auth token to perform request against the portainer API.
 */
const portainerGetAuthToken = () => {
    if (authToken) {
        // Reuse token when available
        return authToken
    } else {
        cy.request({
            method: "POST",
            url: Cypress.config().baseUrl + "/portainer/api/auth/oauth/apiToken",
            body: {
                'Username': env.GetAdminUsername(),
                'Password': env.GetAdminPassword()
            }
        }).then((response) => {
            expect(response.status).to.eq(200)
            if (response.body.jwt) {
                authToken = response.body.jwt
                return response.body.jwt
            } else {
                throw new Error("Valid Bearer Token for Portainer expected.")
            }
        })
    }
}

// ***********************************************
// REST - /api/users
// ***********************************************

/**
 * Retrieves all internal portainer users.
 * Needs to be chained after `portainerGetAuthToken()`
 * @param {String} authToken - A valid auth token for the portainer api.
 * @returns {Object} An object containing all portainer users.
 */
const portainerGetUsers = (authToken) => {
    const Authorization = `Bearer ${authToken}`;
    cy.request({
        method: "GET",
        url: Cypress.config().baseUrl + "/portainer/api/users",
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
 * Deletes the internal portainer user with the given user id.
 * Needs to be chained after `portainerGetAuthToken()`
 * @param {String} authToken - A valid auth token for the portainer api.
 * @param {number} id - The user of the to be deleted user.
 */
const portainerDeleteUser = (authToken, id) => {
    const Authorization = `Bearer ${authToken}`;
    cy.request({
        method: "DELETE",
        url: Cypress.config().baseUrl + `/portainer/api/users/${id}`,
        headers: {
            Authorization
        }
    }).then((response) => {
        expect(response.status).to.eq(204)
    })
}

// ***********************************************
// REST - /api/teams
// ***********************************************

/**
 * Retrieves all teams from portainer.
 * Needs to be chained after `portainerGetAuthToken()`
 * @param {String} authToken - A valid auth token for the portainer api.
 * @returns {Object} - Object containing the team information of all teams.
 */
const portainerGetAllTeams = (authToken) => {
    const Authorization = `Bearer ${authToken}`;
    cy.request({
        method: "GET",
        url: Cypress.config().baseUrl + `/portainer/api/teams`,
        headers: {
            Authorization
        }
    }).then((response) => {
        expect(response.status).to.eq(200)
    })
}

/**
 * Retrieves the team defined by the ID.
 * Needs to be chained after `portainerGetAuthToken()`
 * @param {String} authToken - A valid auth token for the portainer api.
 * @param {number} id - The id of the team.
 * @returns {Object} - Object containing the team's information.
 */
const portainerGetTeamByID = (authToken, id) => {
    const Authorization = `Bearer ${authToken}`;
    cy.request({
        method: "GET",
        url: Cypress.config().baseUrl + `/portainer/api/teams/${id}`,
        headers: {
            Authorization
        }
    }).then((response) => {
        expect(response.status).to.eq(200)
        console.log(JSON.stringify(response.body))
    })
}

/**
 * Retrieves all team names of a user.
 * Needs to be chained after `portainerGetAuthToken()`
 * @param {String} authToken - A valid auth token for the portainer api.
 * @param {number} id - The id of the user.
 * @returns {String[]} - List containing all team names.
 */
const portainerGetTeamNamesForUser = (authToken, id) => {
    const Authorization = `Bearer ${authToken}`;
    cy.request({
        method: "GET",
        url: Cypress.config().baseUrl + `/portainer/api/users/${id}/memberships`,
        headers: {
            Authorization
        }
    }).then((response) => {
        expect(response.status).to.eq(200)

        cy.portainerGetAuthToken().portainerGetAllTeams().then(function (allTeamsList) {
            let stringList = []
            for (let myTeam of response.body) {
                for (let team of allTeamsList.body) {
                    if (myTeam.TeamID === team.Id) {
                        stringList.push(team.Name)
                    }
                }
            }
            return stringList
        })
    })
}

module.exports.register = function () {
    // /api/auth
    Cypress.Commands.add("portainerGetAuthToken", portainerGetAuthToken)

    // /api/users
    Cypress.Commands.add("portainerGetUsers", {prevSubject: true}, portainerGetUsers)
    Cypress.Commands.add("portainerDeleteUser", {prevSubject: true}, portainerDeleteUser)

    // /api/teams
    Cypress.Commands.add("portainerGetAllTeams", {prevSubject: true}, portainerGetAllTeams)
    Cypress.Commands.add("portainerGetTeamByID", {prevSubject: true}, portainerGetTeamByID)
    Cypress.Commands.add("portainerGetTeamNamesForUser", {prevSubject: true}, portainerGetTeamNamesForUser)
}