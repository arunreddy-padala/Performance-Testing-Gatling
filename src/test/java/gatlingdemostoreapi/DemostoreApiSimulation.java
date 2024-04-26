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

  private static class Authentication {


    private static ChainBuilder authenticate =

            exec(
                    http("Authenticate User")
                            .post("/api/authenticate")
                            //Can directly use the JSON within the transaction
                            .body(StringBody("{\n" +
                                    "    \"username\": \"admin\",\n" +
                                    "    \"password\": \"admin\"\n" +
                                    "}"))
//                            .body(RawFileBody("gatlingdemostoreapi/demostoreapisimulation/credentials.json"))
                            //Check if the response returned is 200
                            .check(status().is(200))
                            //Grab the token when the user authenticates and use it for subsequent requests that need it
                            .check(jsonPath("$.token").saveAs("jwt")));

  }

  private static class Categories {

    private static final ChainBuilder list =

            exec(
                    http("List Categories")
                            .get("/api/category")
                            //JSON Path expression check if within our response for id = 6 the name is For Her
                            .check(jsonPath("$[?(@.id == 6)].name").is("For Her")));


    private static final ChainBuilder update =

            exec(
                    http("Update Category")
                            .put("/api/category/7")
                            .headers(authorizationHeader)
                            .body(RawFileBody("gatlingdemostoreapi/demostoreapisimulation/update_category.json"))
                            .check(jsonPath("$.name").is("Everyone")));

  }


  private ScenarioBuilder scn = scenario("DemostoreApiSimulation")
          .exec(
                  exec(Categories.list),
                  pause(2),
                  http("List Products")
                          .get("/api/product?category=7"),
                  pause(2),
                  http("Get a Product")
                          .get("/api/product/34"),
                  pause(2)
                          .exec(Authentication.authenticate),
                  pause(2),
                  http("Updating a Product")
                          .put("/api/product/34")
                          .headers(authorizationHeader)
                          .body(RawFileBody("gatlingdemostoreapi/demostoreapisimulation/update_product.json")),
                  pause(2),
                  http("Create Product 1")
                          .post("/api/product")
                          .headers(authorizationHeader)
                          .body(RawFileBody("gatlingdemostoreapi/demostoreapisimulation/create_product_1.json")),
                  pause(2),
                  http("Create Product 2")
                          .post("/api/product")
                          .headers(authorizationHeader)
                          .body(RawFileBody("gatlingdemostoreapi/demostoreapisimulation/create_product_2.json")),
                  pause(2),
                  http("Create Product 3")
                          .post("/api/product")
                          .headers(authorizationHeader)
                          .body(RawFileBody("gatlingdemostoreapi/demostoreapisimulation/create_product_3.json")),
                  pause(2)
                          .exec(Categories.update)

          );

  {
    setUp(scn.injectOpen(atOnceUsers(1))).protocols(httpProtocol);
  }
}
