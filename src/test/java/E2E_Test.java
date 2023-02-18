import com.sun.org.apache.xerces.internal.util.SynchronizedSymbolTable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.junit.Assert;
import org.json.JSONStringer;

import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import com.github.javafaker.Faker;

import java.time.format.DateTimeFormatter;
import java.time.LocalDate;

public class E2E_Test {



    public static void main(String[] args){
        Faker faker = new Faker();
        String newFirstName = faker.name().firstName();
        String newLastName = faker.name().lastName();
        String username = "admin";
        String password = "password123";
        String baseUrl = "https://restful-booker.herokuapp.com";

        RestAssured.baseURI = baseUrl;
        RequestSpecification request = RestAssured.given();

        //Test starts by getting the token for auth
        request.header("Content-Type", "application/json");
        request.header("accept","application/json");

        Response response = request.body("{ \"username\":\"" + username + "\", \"password\":\"" + password + "\"}")
            .post("/auth");

        Assert.assertEquals(200,  response.statusCode());

        String responseString = response.asString();

        //grab token for subsequent requests
        String token = JsonPath.from(responseString).get("token");

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate today = LocalDate.now();

        String checkIn = "2020-01-01";
        String checkOut = "2023-12-09";

        // get all booking Ids
        response = request.get("/booking?checkin="+checkIn+"&checkout="+checkOut);

        Map<String, Integer> bookingInfo = response.jsonPath().getMap("[0]");

        int bookingId = bookingInfo.get("bookingid");



        // get a specific booking id and check values

        response = request.get("/booking/"+bookingId);

        Assert.assertEquals(200, response.statusCode());

        String bookingDetailsFirstName = response.jsonPath().getString("firstname");
        String bookingDetailsLastName = response.jsonPath().getString("lastname");
        int bookingDetailsPrice = response.jsonPath().getInt("totalprice");


        Assert.assertTrue(bookingDetailsFirstName != null);
        Assert.assertTrue(bookingDetailsLastName != null);
        Assert.assertTrue(bookingDetailsPrice > 0);



        // partial update depositpaid boolean for booking id

        // 1. use token in the header
        request.header("Cookie","token="+token);


        String requestString = new JSONStringer()
            .object()
            .key("firstname")
            .value(newFirstName)
            .key("lastname")
            .value(newLastName)
            .endObject()
            .toString();


        response = request.body(requestString).patch("/booking/"+bookingId);

        String bookingFirstName = response.jsonPath().getString("firstname");
        String bookingLastName = response.jsonPath().getString("lastname");

        Assert.assertEquals(200, response.statusCode());

        Assert.assertTrue(newFirstName.equals(bookingFirstName));

        Assert.assertTrue(newLastName.equals(bookingLastName));


        // Create a new booking

        // 1. Generate fake test data
        String newBookingFirstName = faker.name().firstName();
        String newBookingLastName = faker.name().lastName();
        Double newBookingPrice = Double.valueOf(faker.commerce().price());
        String newBookingCheckIn = dtf.format(today);
        String newBookingCheckOut = dtf.format(today.plusDays(10));
        String newBookingNeeds = faker.commerce().productName();

        // 2. create Json object for booking dates checkin & checkout
         JSONObject newBookingDates = new JSONObject();

         newBookingDates.put("checkin",newBookingCheckIn);
         newBookingDates.put("checkout",newBookingCheckOut);

         // 3. create the object that will be sent as the request
        String newBookingObject = new JSONStringer()
            .object()
            .key("firstname")
            .value(newBookingFirstName)
            .key("lastname")
            .value(newBookingLastName)
            .key("totalprice")
            .value(newBookingPrice)
            .key("depositpaid")
            .value(true)
            .key("bookingdates")
            .value(newBookingDates)
            .key("additionalneeds")
            .value(newBookingNeeds)
            .endObject()
            .toString();


        //4. make POST request
        response =  request.body(newBookingObject).post("/booking");

        String responseBookingFirstName = response.jsonPath().getString("booking.firstname");
        String responseBookingLastName = response.jsonPath().getString("booking.lastname");

        // 5. make assertions confirming data returned is as expected
        Assert.assertEquals(200, response.statusCode());
        Assert.assertNotNull(response.jsonPath().getString("bookingid"));
        Assert.assertTrue(newBookingFirstName.equals(responseBookingFirstName));
        Assert.assertTrue(newBookingLastName.equals(responseBookingLastName));



        // Delete a random booking


        // 1. Get first booking id

        response =  request.get("/booking");
        bookingId = response.jsonPath().getInt("[0].bookingid");

        // 2. make api call to DELETE the booking
        response = request.delete("/booking/"+bookingId);

        // 2.
        Assert.assertEquals(201, response.statusCode());



    }




}
