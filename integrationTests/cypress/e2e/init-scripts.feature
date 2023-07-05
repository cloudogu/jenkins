Feature: Init scripts tests

  @requires_testuser @hide_warp_menu
  Scenario: Logged in admin user can view maven M3
    Given the user is member of the admin user group
    When the user logs into the CES
    And the user navigates to tool configuration admin page
    Then the user sees the maven m3 installation