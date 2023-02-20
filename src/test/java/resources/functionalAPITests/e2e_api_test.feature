Feature: End to End Tests for the restful-booker API (https://restful-booker.herokuapp.com/apidoc/index.html)

  Background: User generates an authorization token
    Given I am an authorized user

  Scenario: A user with an authentication token can update a booking
    Given I have a booking id
    When I change the first name and last name on a booking
    Then the updated first name and last name appears





