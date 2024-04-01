package gatlingdemostore;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import static io.gatling.javaapi.core.CoreDsl.atOnceUsers;
import static io.gatling.javaapi.core.CoreDsl.css;
import static io.gatling.javaapi.core.CoreDsl.regex;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;

public class DemoStoreSimulation extends Simulation {

  private static final String DOMAIN = "demostore.gatling.io";
  private static final HttpProtocolBuilder HTTP_PROTOCOL = http.baseUrl("https://" + DOMAIN);

  {

    String uri1 = "demostore.gatling.io";

    ScenarioBuilder scn =
            scenario("DemostoreSimulation")
                    .exec(http("Load Home Page")
                            .get("/")
                            //Check to make sure home page loads correctly
                            .check(regex("<title>Gatling Demo-Store</title>").exists())
                            //Using css check to capture the csrf value and to be used in request 6
                            .check(css("#_csrf", "content").saveAs("csrfValue")))
                    .pause(2)
                    .exec(http("Load About Us")
                            .get("/about-us"))
                    .pause(2)
                    .exec(http("Load Categories")
                            .get("/category/all"))
                    .pause(2)
                    .exec(http("Load Product Details")
                            .get("/product/black-and-red-glasses"))
                    .pause(2)
                    .exec(http("Add Product to Cart")
                            .get("/cart/add/19"))
                    .pause(2)
                    .exec(http("View Cart")
                            .get("/cart/view"))
                    .pause(2)
                    .exec(
                            http("Login User")
                                    .post("http://" + uri1 + "/login")
                                    //Remove the hardcoded csrf value and use the captured value in home page request
                                    .formParam("_csrf", "#{csrfValue}")
                                    .formParam("username", "user1")
                                    .formParam("password", "pass"))
                    .pause(2)
                    .exec(http("Checkout Cart").get("http://" + uri1 + "/cart/checkout"));

    setUp(scn.injectOpen(atOnceUsers(1))).protocols(HTTP_PROTOCOL);
  }
}