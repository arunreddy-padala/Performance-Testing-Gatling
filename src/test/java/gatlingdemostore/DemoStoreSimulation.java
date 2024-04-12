package gatlingdemostore;

import java.util.concurrent.ThreadLocalRandom;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.FeederBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import static io.gatling.javaapi.core.CoreDsl.atOnceUsers;
import static io.gatling.javaapi.core.CoreDsl.css;
import static io.gatling.javaapi.core.CoreDsl.csv;
import static io.gatling.javaapi.core.CoreDsl.doIf;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.feed;
import static io.gatling.javaapi.core.CoreDsl.jsonFile;
import static io.gatling.javaapi.core.CoreDsl.regex;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.core.CoreDsl.substring;
import static io.gatling.javaapi.http.HttpDsl.Cookie;
import static io.gatling.javaapi.http.HttpDsl.addCookie;
import static io.gatling.javaapi.http.HttpDsl.flushCookieJar;
import static io.gatling.javaapi.http.HttpDsl.http;

public class DemoStoreSimulation extends Simulation {

  private static final String DOMAIN = "demostore.gatling.io";
  private static final HttpProtocolBuilder HTTP_PROTOCOL = http.baseUrl("https://" + DOMAIN);

  //Using external csv datasource for parameter values
  private static final FeederBuilder<String> categoryFeeder =
          csv("/Users/arunkumarreddy/Documents/Projects/Project-Gatling/gatling-demostore/src/test/resources/data/categoryDetails.csv")
                  .random();

  private static final FeederBuilder<Object> JsonFeederProducts =
          jsonFile("/Users/arunkumarreddy/Documents/Projects/Project-Gatling/gatling-demostore/src/test/resources/data/productDetails.json")
                  .random();

  private static final FeederBuilder<String> loginFeeder =
          csv("/Users/arunkumarreddy/Documents/Projects/Project-Gatling/gatling-demostore/src/test/resources/data/loginDetails.csv")
                  .circular();

  private static final ChainBuilder initSession =

          exec(
                  //Empty cookies for each VU
                  flushCookieJar())
                  //Setting a random number within our session
                  .exec(session -> session.set("randomNumber", ThreadLocalRandom.current().nextInt()))
                  //Session variable for customer logged in or not; initially not logged in set to false
                  .exec(session -> session.set("customerLoggedIn", false))
                  //Session variable to keep track of cart total
                  .exec(session -> session.set("cartTotal", 0.00))
                  //Generate a new cookie with a session id using a helper class
                  .exec(addCookie(Cookie("sessionId", SessionId.random()).withDomain(DOMAIN)));

  private static final String uri1 = "demostore.gatling.io";


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

    private static class Product {
      private static final ChainBuilder view =

              feed(JsonFeederProducts)
                      .exec(
                              http("Load product page - #{name}")
                                      .get("/product/#{slug}")
                                      .check(css("#ProductDescription").isEL("#{description}")));


      private static final ChainBuilder add =

              //Call the above product view chain builder and then process the below chain
              exec(view).
                      exec(
                              http("Add Product to Cart")
                                      .get("/cart/add/#{id}")
                                      .check(substring("items in your cart")))

                      //Calculating the cart total for the products
                      .exec(
                              session -> {

                                double cartTotalCurrent = session.getDouble("cartTotal");
                                //Price value extracted from JSON data
                                double itemPrice = session.getDouble("price");
                                return session.set("cartTotal", cartTotalCurrent + itemPrice);

                              }


                      );


    }

  }

  private static class Customer {
    private static final ChainBuilder login =

            feed(loginFeeder)
                    .exec(
                            http("Load login page")
                                    .get("/login")
                                    //Css check to see if we are in the login page
                                    .check(substring("Username:")))

                    //Print out the customerLoggedIn session value
                    .exec(

                            session -> {

                              System.out.println("Customer Logged in: " + session.get("customerLoggedIn").toString());
                              return session;

                            }

                    )

                    .exec(
                            http("Customer Login Action")
                                    .post("http://" + uri1 + "/login")
                                    .formParam("_csrf", "#{csrfValue}")
                                    .formParam("username", "#{username}")
                                    .formParam("password", "#{password}"))
                    .exec(session -> session.set("customerLoggedIn", true))
                    //Print out the customerLoggedIn session value
                    .exec(

                            session -> {

                              System.out.println("Customer Logged in: " + session.get("customerLoggedIn").toString());
                              return session;

                            }

                    );


  }

  private static class Checkout {
    private static final ChainBuilder ViewCart =
            //If the customer isn't logged in then redirect to login transaction
            doIf(session -> !session.getBoolean("customerLoggedIn"))
                    .then(exec(Customer.login))
                    .exec(
                            http("Viewing the Cart Page")
                                    .get("/cart/view")
                                    //Check if the checkout total is equal to the total we calculated in product
                                    .check(css("#grandTotal").isEL("$#{cartTotal}")))
                    .exec(

                            session -> {

                              System.out.println("Cart Total is: " + session.get("cartTotal").toString());
                              return session;

                            }

                    );


    private static final ChainBuilder CompleteCheckoutView =

            exec(
                    http("Checkout Cart")
                            .get("http://" + uri1 + "/cart/checkout")
                            .check(substring("Thanks for your order! See you soon!")));

  }


  {

    ScenarioBuilder scn =
            scenario("DemostoreSimulation")
                    .exec(initSession)
                    .exec(CmsPage.homePage)
                    .pause(2)
                    .exec(CmsPage.aboutUs)
                    .pause(2)
                    .exec(Catalog.Category.view)
                    .pause(2)
                    .exec(Catalog.Product.add)
                    .pause(2)
                    .exec(Checkout.ViewCart)
                    //Code handled within the cart checkout transaction
//                    .pause(2)
//                    .exec(Customer.login)
                    .pause(2)
                    .exec(Checkout.CompleteCheckoutView);

    setUp(scn.injectOpen(atOnceUsers(1))).protocols(HTTP_PROTOCOL);

  }
}