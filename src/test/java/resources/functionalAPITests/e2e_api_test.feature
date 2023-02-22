Feature: End to End Tests for the restful-booker API (https://restful-booker.herokuapp.com/apidoc/index.html)

  Background: User generates an authorization token
    Given I am an authorized user

  Scenario: A user with an authentication token can partially update a booking
    Given I have a booking id
    When I change the first name and last name on a booking
    Then the updated first name and last name appears


  Scenario: A user can create a new booking
    When I create a booking
    Then the request is successful
    And a booking id is returned


  Scenario: A user can update a booking
    When I have a booking id
    And I update all of the booking details
    Then the request is successful