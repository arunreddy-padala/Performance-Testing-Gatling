package gatlingdemostore;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

import gatlingdemostore.pageObjects.Catalog;
import gatlingdemostore.pageObjects.Checkout;
import gatlingdemostore.pageObjects.CmsPages;
import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.Choice;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.rampUsers;
import static io.gatling.javaapi.core.CoreDsl.randomSwitch;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.Cookie;
import static io.gatling.javaapi.http.HttpDsl.addCookie;
import static io.gatling.javaapi.http.HttpDsl.flushCookieJar;
import static io.gatling.javaapi.http.HttpDsl.http;

public class DemoStoreSimulation extends Simulation {

  private static final String DOMAIN = "demostore.gatling.io";
  private static final HttpProtocolBuilder HTTP_PROTOCOL = http.baseUrl("https://" + DOMAIN);

  //Runtime properties that define the user count for simulation or default to 5 users
  private static final int USER_COUNT = Integer.parseInt(System.getProperty("USERS", "3"));

  private static final Duration RAMP_DURATION = Duration.ofSeconds(Integer.parseInt(System.getProperty("RAMP_DURATION", "10")));

  private static final Duration TEST_DURATION = Duration.ofSeconds(Integer.parseInt(System.getProperty("TEST_DURATION", "60")));

  @Override
//Prints the values before running the test
  public void before() {
    System.out.printf("Running test with %d users%n", USER_COUNT);
    System.out.printf("Ramping users over with %d seconds%n", RAMP_DURATION.getSeconds());
    System.out.printf("Total test duration with %d seconds%n", TEST_DURATION.getSeconds());

  }

  @Override
//Prints the values after running the test
  public void after() {
    System.out.println("Stress testing is now complete");

  }


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


  private static class UserJourneys {

    private static final Duration MIN_PAUSE = Duration.ofMillis(100);
    private static final Duration MAX_PAUSE = Duration.ofMillis(500);

    private static final ChainBuilder browseStore =

            exec(
                    initSession)
                    .exec(CmsPages.homePage)
                    .pause(MAX_PAUSE)
                    .exec(CmsPages.aboutUs)
                    //Chose random pause between the two
                    .pause(MIN_PAUSE, MAX_PAUSE)
                    //Repeat the below 5 times
                    .repeat(5)
                    .on(
                            exec(Catalog.Category.view)
                                    .pause(MIN_PAUSE, MAX_PAUSE)
                                    .exec((Catalog.Product.view)));

    private static final ChainBuilder abandonCart =

            exec(
                    initSession)
                    .exec(CmsPages.homePage)
                    .pause(MAX_PAUSE)
                    .exec(Catalog.Category.view)
                    .pause(MIN_PAUSE, MAX_PAUSE)
                    .exec(Catalog.Product.view)
                    .pause(MIN_PAUSE, MAX_PAUSE)
                    .exec(Catalog.Product.add);

    private static final ChainBuilder completePurchase =

            exec(
                    initSession)
                    .exec(CmsPages.homePage)
                    .pause(MAX_PAUSE)
                    .exec(Catalog.Category.view)
                    .pause(MIN_PAUSE, MAX_PAUSE)
                    .exec(Catalog.Product.view)
                    .pause(MIN_PAUSE, MAX_PAUSE)
                    .exec(Catalog.Product.add)
                    .pause(MIN_PAUSE, MAX_PAUSE)
                    .exec(Checkout.ViewCart)
                    .pause(MIN_PAUSE, MAX_PAUSE)
                    .exec(Checkout.CompleteCheckoutView);

    private static final ChainBuilder demostoreSimulation =

            exec(initSession)
                    .exec(CmsPages.homePage)
                    .pause(2)
                    .exec(CmsPages.aboutUs)
                    .pause(2)
                    .exec(Catalog.Category.view)
                    .pause(2)
                    .exec(Catalog.Product.add)
                    .pause(2)
                    .exec(Checkout.ViewCart)
                    .pause(2)
                    .exec(Checkout.CompleteCheckoutView);

  }

  private static class Scenarios {

    private static final ScenarioBuilder defaultPurchase =

            scenario("Default Load Test Scenario")
                    .during(TEST_DURATION)
                    .on(
                            //Randomly execute these scenarios based on the weights
                            randomSwitch()
                                    .on(

                                            //Ex: Simulating the scenario where 75% of the users browse the store
                                            new Choice.WithWeight(75.0, exec(UserJourneys.browseStore)),
                                            new Choice.WithWeight(15.0, exec(UserJourneys.abandonCart)),
                                            new Choice.WithWeight(10.0, exec(UserJourneys.completePurchase))));


    private static final ScenarioBuilder highPurchase =

            scenario("High Purchase Load Test Scenario")
                    .during(Duration.ofSeconds(60))
                    .on(
                            //Randomly execute these scenarios based on the weights
                            randomSwitch()
                                    .on(

                                            new Choice.WithWeight(25.0, exec(UserJourneys.browseStore)),
                                            new Choice.WithWeight(25.0, exec(UserJourneys.abandonCart)),
                                            new Choice.WithWeight(50.0, exec(UserJourneys.completePurchase))));


    private static final ScenarioBuilder initialScenario =

            scenario("Initial Load Test Scenario")
                    .during(Duration.ofSeconds(60))
                    .on(
                            randomSwitch()
                                    .on(
                                            new Choice.WithWeight(100.0, exec(UserJourneys.demostoreSimulation))));

  }


  {

    /* Sequential Execution
    setUp(

            Scenarios.defaultPurchase.injectOpen(
                            rampUsers(USER_COUNT)
                                    .during(RAMP_DURATION))
                    .protocols((HTTP_PROTOCOL)).
                    andThen(
                            Scenarios.highPurchase.injectOpen(
                                            rampUsers(5)
                                                    .during(Duration.ofSeconds(10)))
                                    .protocols(HTTP_PROTOCOL)));

     */


    //Parallel Execution
    setUp(

            Scenarios.defaultPurchase.injectOpen(rampUsers(USER_COUNT).during(RAMP_DURATION)),
            Scenarios.highPurchase.injectOpen(rampUsers(2).during(Duration.ofSeconds(10))))
            .protocols(HTTP_PROTOCOL);


  }
}