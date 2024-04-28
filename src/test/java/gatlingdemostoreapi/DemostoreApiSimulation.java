package gatlingdemostoreapi;

import java.util.*;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public class DemostoreApiSimulation extends Simulation {

  private final HttpProtocolBuilder httpProtocol = http
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

    //Feeder for Categories data
    private static final FeederBuilder.Batchable<String> categoriesFeeder =
            csv("data/categories.csv").random();

    private static final ChainBuilder list =

            exec(
                    http("List Categories")
                            .get("/api/category")
                            //Return a array of strings
                            .check(jmesPath("[? id == `6`].name").ofList().is(List.of("For Her"))));


    private static final ChainBuilder update =

            //Use the feeder
            feed(categoriesFeeder)
                    //Call the auth method first and then update the category
                    .exec(Authentication.authenticate)
                    .exec(
                            http("Update Category")
                                    .put("/api/category/#{categoryId}")
                                    .headers(authorizationHeader)
                                    //The RawFileBody content cannot be used with # session variables we need to use EL for the newer syntax
                                    .body(ElFileBody("gatlingdemostoreapi/demostoreapisimulation/update_category.json"))
                                    //Changed to EL as it's evaluating an expression
                                    .check(jsonPath("$.name").isEL("#{categoryName}")));

  }

  private static class Products {

    //Feeder for Categories data
    private static final FeederBuilder.Batchable<String> productsFeeder =
            csv("data/products.csv").circular();

    private static final ChainBuilder list =

            feed(productsFeeder)
                    .exec(
                            http("List Products")
                                    .get("/api/product?category=#{productCategoryId}")
                                    //Check if products with Category id 7 shouldn't appear
                                    .check(jsonPath("$[?(@.categoryId != \"#{productCategoryId}\")]").notExists())
                                    //Save the return list and use it in subsequent calls
                                    .check(jmesPath("[*].id").ofList().saveAs("allProductIds")));


    private static final ChainBuilder get =

            //Using session variables store the product is in a list and randomly get one
            exec(session -> {
              List<Integer> allProductIds = session.getList("allProductIds");
              return session.set("productId", allProductIds.get(new Random().nextInt(allProductIds.size())));
            })
                    .exec(

                            session -> {
                              //Array of ids
                              System.out.println("allProductIds " + session.get("allProductIds").toString());
                              //Randomly selected id from array
                              System.out.println("productId selected " + session.get("productId").toString());
                              return session;
                            }
                    )

                    .exec(
                            http("Get a Product")
                                    //Using the above randomly selected productId
                                    .get("/api/product/#{productId}")
                                    .check(jmesPath("id").ofInt().isEL("#{productId}"))
                                    //Capture the entire json of the get call and save as product
                                    .check(jmesPath("@").ofMap().saveAs("product")))

                    .exec(
                            session -> {

                              //Print the product obtained as part of GET call
                              System.out.println("Product is " + session.get("product").toString());
                              return session;
                            }


                    );


    private static final ChainBuilder update =

            /*
             * Simulating an actual user request where we receive a response using GET
             * Update the product using a PUT
             * */

            //Call the auth method first and then update the product
            exec(Authentication.authenticate)

                    .exec(session -> {

                      Map<String, Object> product = session.getMap("product");
                      //Using the GET json value to set the session variables so that they can be reused
                      return session.set("productCategoryId", product.get("categoryId"))
                              .set("productName", product.get("name"))
                              .set("productDescription", product.get("description"))
                              .set("productImage", product.get("image"))
                              .set("productPrice", product.get("price"))
                              .set("productId", product.get("id"));

                    })


                    .exec(
                            http("Updating a Product #{productName}")
                                    .put("/api/product/#{productId}")
                                    .headers(authorizationHeader)
                                    //Using the template json file as our body request
                                    .body(ElFileBody("gatlingdemostoreapi/demostoreapisimulation/create_product.json"))
                                    .check(jsonPath("$.price").isEL("#{productPrice}")));


    private static final ChainBuilder create =


            //Call the auth method first and then create the product
            exec(Authentication.authenticate)
                    .feed(productsFeeder)
                    .exec(
                            http("Create Product #{productName}")
                                    .post("/api/product")
                                    .headers(authorizationHeader)
                                    //Using a template to create a product and using the feeder inside the template to inject data
                                    .body(ElFileBody("gatlingdemostoreapi/demostoreapisimulation/create_product.json")));


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
                          .repeat(3).on(exec(Products.create)),
                  pause(2)
                          /*Create 3 products using repeat DSL block */
                          .exec(Categories.update)

          );

  {
    setUp(scn.injectOpen(atOnceUsers(1))).protocols(httpProtocol);
  }
}
