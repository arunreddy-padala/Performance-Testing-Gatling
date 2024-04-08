package gatlingdemostore;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.FeederBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;

public class DemoStoreSimulation extends Simulation {

  private static final String DOMAIN = "demostore.gatling.io";
  private static final HttpProtocolBuilder HTTP_PROTOCOL = http.baseUrl("https://" + DOMAIN);

  //Using external csv datasource for parameter values
  private static final FeederBuilder<String> categoryFeeder =
          csv("/Users/arunkumarreddy/Documents/Projects/Project-Gatling/gatling-demostore/src/test/resources/data/categoryDetails.csv")
                  .random();

  private static class CmsPage {

    private static final ChainBuilder homePage =
            exec(http("Load Home Page")
                    .get("/")
                    //Check to make sure home page loads correctly
                    .check(regex("<title>Gatling Demo-Store</title>").exists())
                    //Using css check to capture the csrf value and to be used in request 6
                    .check(css("#_csrf", "content").saveAs("csrfValue")));


    private static final ChainBuilder aboutUs =

            exec(http("Load About Us")
                    .get("/about-us")
                    //Check to ensure the About Us title can be retrieved from the page
                    .check(substring("About Us")));

  }

  private static class Catalog {
    private static class Category {
      private static final ChainBuilder view =
              feed(categoryFeeder)
                      .exec(
                              //Parameterize the category type that we want to get
                              http("Load Category Page - #{categoryName}")
                                      .get("/category/#{categorySlug}")
                                      //Check within the extracted page if the css selector CategoryName matches our parameter value
                                      .check(css("#CategoryName").isEL("#{categoryName}")));


    }

  }


  {

    String uri1 = "demostore.gatling.io";

    ScenarioBuilder scn =
            scenario("DemostoreSimulation")
                    .exec(CmsPage.homePage)
                    .pause(2)
                    .exec(CmsPage.aboutUs)
                    .pause(2)
                    .exec(Catalog.Category.view)
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