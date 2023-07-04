Feature: Jenkins Rest Api

  @requires_testuser
  Scenario: Logged out user can use his apikey
    Given the user is logged into the CES
    And the user generates an apikey
    Then the user can use his apikey