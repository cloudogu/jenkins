Feature: Automatic grant of privileges when logging into a dogu (gui)

  @requires_testuser
  Scenario: ces user with default privileges has default privileges in the dogu
    Given the user is not member of the admin user group
    When the user logs into the CES
    Then the user has no administrator privileges in the dogu gui

  @requires_testuser
  Scenario: ces user with admin privileges has admin privileges in the dogu
    Given the user is member of the admin user group
    When the user logs into the CES
    Then the user has administrator privileges in the dogu gui