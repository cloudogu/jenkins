Feature: Jenkins Rest Api

  @requires_testuser @hide_warp_menu
  Scenario: Logged out user can use his apikey
    Given the user is logged out of the CES
    When the user opens the dogu start page
    And the user types in correct login credentials
    And the user clicks the login button
    And the user generates an apikey
    Then the user can use his apikey