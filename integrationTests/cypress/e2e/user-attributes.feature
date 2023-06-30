Feature: Jenkins user attributes

  @requires_testuser @hide_warp_menu
  Scenario: Logged in user can see his attributes (front channel)
    Given the user is logged out of the CES
    When the user opens the dogu start page
    And the user types in correct login credentials
    And the user clicks the login button
    Then the user can view his attributes with profile page

  @requires_testuser @hide_warp_menu
  Scenario: Logged in user can see his attributes (back channel)
    Given the user is logged out of the CES
    When the user opens the dogu start page
    And the user types in correct login credentials
    And the user clicks the login button
    And the user generates an apikey
    Then the user can view his attributes with rest