Feature: Jenkins user attributes

  @requires_testuser
  Scenario: Logged in user can see his attributes (front channel)
    Given the user is logged into the CES
    Then the user can view his attributes with profile page

  @requires_testuser
  Scenario: Logged in user can see his attributes (back channel)
    Given the user is logged into the CES
    Then the user can view his attributes with rest