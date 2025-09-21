Feature: User login
  Scenario: Successful login
    Given a user "alice" exists with password "secret"
    When I login with username "alice" and password "secret"
    Then I should receive a token

  Scenario: Wrong password
    Given a user "alice" exists with password "secret"
    When I login with username "alice" and password "oops"
    Then I should see a bad request error
