package gatlingdemostoreapi;

import java.util.*;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public class DemostoreApiSimulation extends Simulation {

  private HttpProtocolBuilder httpProtocol = http
          .baseUrl("https://demostore.gatling.io")
          .header("Cache-Control", "no-cache")
          .contentTypeHeader("application/json")
          .acceptHeader("application/json");
  private static final Map<CharSequence, String> authorizationHeader = Map.ofEntries(
          //Use the capture token in api/authentication transaction
          Map.entry("authorization", "Bearer #{jwt}")
  );

  private static final ChainBuilder initSession =

          //Session for authentication
          exec(session -> session.set("authenticated", false));


  private static class Authentication {


    private static final ChainBuilder authenticate =

            //run the authenticate code only if it's false i.e the user is not authenticated
            doIf(session -> !session.getBoolean("authenticated")).then(

                    exec(
                            http("Authenticate User")
                                    .post("/api/authenticate")
                                    //Can directly use the JSON within the transaction instead of RawFileBody
                                    .body(StringBody("{\n" +
                                            "    \"username\": \"admin\",\n" +
                                            "    \"password\": \"admin\"\n" +
                                            "}"))
                                    //Check if the response returned is 200
                                    .check(status().is(200))
                                    //Using jmesPath instead of jsonPath
                                    .check(jmesPath("token").saveAs("jwt")))
                            //Set the user as authenticated once the above code block is run
                            .exec(session -> session.set("authenticated", true))


            );


  }

  private static class Categories {

    private static final ChainBuilder list =

            exec(
                    http("List Categories")
                            .get("/api/category")
                            //Return a array of strings
                            .check(jmesPath("[? id == `6`].name").ofList().is(List.of("For Her"))));


    private static final ChainBuilder update =

            //Call the auth method first and then update the category
            exec(Authentication.authenticate)
                    .exec(
                            http("Update Category")
                                    .put("/api/category/7")
                                    .headers(authorizationHeader)
                                    .body(RawFileBody("gatlingdemostoreapi/demostoreapisimulation/update_category.json"))
                                    .check(jsonPath("$.name").is("Everyone")));

  }

  private static class Products {

    private static final ChainBuilder list =

            exec(
                    http("List Products")
                            .get("/api/product?category=7")
                            //Check if products with Category id 7 shouldn't appear
                            .check(jsonPath("$[?(@.categoryId != \"7\")]").notExists()));


    private static final ChainBuilder get =

            exec(
                    http("Get a Product")
                            .get("/api/product/34")
                            .check(jsonPath("$.id").ofInt().is(34)));


    private static final ChainBuilder update =

            //Call the auth method first and then update the product
            exec(Authentication.authenticate)
                    .exec(
                            http("Updating a Product")
                                    .put("/api/product/34")
                                    .headers(authorizationHeader)
                                    .body(RawFileBody("gatlingdemostoreapi/demostoreapisimulation/update_product.json"))
                                    .check(jsonPath("$.price").is("15.99")));


    private static final ChainBuilder create =

            /*Create 3 products using 3 repeat DSL block,
            productCount is our counter (starts from 0) which will increment when the repeat block runs*/

            //Call the auth method first and then create the product
            exec(Authentication.authenticate)
                    .repeat(3, "productCount").on(

                            exec(
                                    http("Create Product #{productCount}")
                                            .post("/api/product")
                                            .headers(authorizationHeader)
                                            .body(RawFileBody("gatlingdemostoreapi/demostoreapisimulation/create_product_#{productCount}.json"))));


  }


  private ScenarioBuilder scn = scenario("DemostoreApiSimulation")
          .exec(
                  exec(initSession),
                  exec(Categories.list),
                  pause(2)
                          .exec(Products.list),
                  pause(2)
                          .exec(Products.get).
                          pause(2),
                  pause(2)
                          .exec(Products.update),
                  pause(2)
                          .exec(Products.create),
                  pause(2)
                          .exec(Categories.update)

          );

  {
    setUp(scn.injectOpen(atOnceUsers(1))).protocols(httpProtocol);
  }
}
