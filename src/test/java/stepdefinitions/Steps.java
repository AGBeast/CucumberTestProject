package stepdefinitions;

import com.github.javafaker.Faker;
import io.cucumber.java.bs.A.As;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.json.JSONObject;
import org.junit.Assert;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class Steps {

  private static final String BASE_URL = "https://restful-booker.herokuapp.com";
  private static final String USER_NAME = "admin";
  private static final String PASSWORD  = "password123";

  private String newBookingFirstName;
  private String newBookingLastName;


  private static String authToken;
  private static Response apiResponse;
  private static int bookingId;



  @Given("I am an authorized user")
  public void i_am_an_authorized_user(){
    RestAssured.baseURI = BASE_URL;
    RequestSpecification request = RestAssured.given()
        .header("Content-Type", "application/json")
        .header("Accept","application/json");

    JSONObject authRequest = new JSONObject();
    authRequest.put("username",USER_NAME);
    authRequest.put("password",PASSWORD);


    apiResponse = request.body(authRequest.toString()).post("/auth");
    authToken = apiResponse.jsonPath().getString("token");

    Assert.assertEquals(200, apiResponse.statusCode());
    Assert.assertNotNull(authToken);


  }

  @Given("I have a booking id")
  public void i_have_a_booking_id() {
    RestAssured.baseURI = BASE_URL;
    RequestSpecification request = RestAssured.given()
        .header("Content-Type", "application/json")
        .header("Accept","application/json");

    apiResponse = request.get("/booking");
    Map<String, Integer> bookingIdsMap = apiResponse.jsonPath().getMap("[0]");
    Collection<Integer> bookingIds = bookingIdsMap.values();

    Assert.assertEquals(200, apiResponse.statusCode());
    Assert.assertFalse(bookingIds.isEmpty());

    bookingId = bookingIds.iterator().next();

  }
  @When("I change the first name and last name on a booking")
  public void i_change_the_first_name_and_last_name_on_a_booking() {
    RestAssured.baseURI = BASE_URL;
    RequestSpecification request = RestAssured.given()
        .header("Content-Type", "application/json")
        .header("Accept","application/json")
        .header("Cookie","token="+authToken);

    Faker faker = new Faker();

    newBookingFirstName = faker.name().firstName();
    newBookingLastName = faker.name().lastName();

    JSONObject patchRequestObj = new JSONObject();
    patchRequestObj.put("firstname",newBookingFirstName);
    patchRequestObj.put("lastname",newBookingLastName);

    apiResponse = request.body(patchRequestObj.toString()).patch("/booking/"+bookingId);

    Assert.assertEquals(200, apiResponse.statusCode());


  }

  @Then("the updated first name and last name appears")
  public void the_updated_first_name_and_last_name_appears() {
    Assert.assertTrue(newBookingFirstName.equals(apiResponse.jsonPath().getString("firstname")));
    Assert.assertTrue(newBookingLastName.equals(apiResponse.jsonPath().getString("lastname")));
  }



}
