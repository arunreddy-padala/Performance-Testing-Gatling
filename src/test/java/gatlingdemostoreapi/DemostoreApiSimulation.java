package gatlingdemostoreapi;

import java.time.Duration;
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

  /*
  * Run time parameters if not passed default is used  */
  private static final int USER_COUNT = Integer.parseInt(System.getProperty("USERS", "5"));
  private static final Duration RAMP_DURATION = Duration.ofSeconds(Integer.parseInt(System.getProperty("RAMP_DURATION", "10")));
  private static final Duration TEST_DURATION = Duration.ofSeconds(Integer.parseInt(System.getProperty("TEST_DURATION", "60")));

  /*
Code that executes before the test is run
 */
  @Override
  public void before(){

    System.out.printf("Running test with %d users%n", USER_COUNT);
    System.out.printf("Ramping users over %d seconds%n", RAMP_DURATION.getSeconds());
    System.out.printf("Test duration %d seconds%n", TEST_DURATION.getSeconds());

  }

/*
Code that executes after the test is run
 */
  @Override
  public void after(){

    System.out.println("Stress test completed");

  }
  private static final ChainBuilder initSession =

          //Session for authentication
          exec(session -> session.set("authenticated", false));


  private static class UserJourneys {

    private static Duration minPause = Duration.ofMillis(200);
    private static Duration maxPause = Duration.ofSeconds(3);

    public static ChainBuilder admin =

            exec(initSession)
                    .exec(Categories.list)
                    .pause(minPause, maxPause)
                    .exec(Products.list)
                    .pause(minPause, maxPause)
                    .exec(Products.get)
                    .pause(minPause, maxPause)
                    .exec(Products.update)
                    .pause(minPause, maxPause)
                    .repeat(3).on(exec(Products.create))
                    .pause(minPause, maxPause)
                    .exec(Categories.update);

    /* User Journey to scrape products and prices */

    public static ChainBuilder priceScrapper =

            exec(Categories.list)
                    .pause(minPause, maxPause)
                    .exec(Products.listAll);

    /* User Journey to update product price for the products that we get */
    public static ChainBuilder priceUpdate =


            exec(initSession)
                    .exec(Products.listAll)
                    .pause(minPause, maxPause)
                    .repeat("#{allProducts.size()}", "productIndex")
                    .on(
                            exec(session -> {
                              int index = session.getInt("productIndex");
                              List<Object> allProducts = session.getList("allProducts");
                              return session.set("product", allProducts.get(index));
                            })

                                    .exec(Products.update)
                                    .pause(minPause, maxPause));


  }

  private static class Scenarios {

    public static ScenarioBuilder defaultScn = scenario("Default Scenario Test")
            .during(TEST_DURATION)
            .on(
                    randomSwitch().on(
                            // Execute 20% of time admin scenario
                            Choice.withWeight(20d, exec(UserJourneys.admin)),
                            // Execute 40% of time price scrapper scenario
                            Choice.withWeight(40d, exec(UserJourneys.priceScrapper)),
                            Choice.withWeight(40d, exec(UserJourneys.priceUpdate))


                    )

            );

    public static ScenarioBuilder noAdminScn = scenario("Load test without Admin")
            .during(Duration.ofSeconds(60))
            .on(
                    randomSwitch().on(
                            // Execute 60% of time price scrapper scenario
                            Choice.withWeight(40d, exec(UserJourneys.priceScrapper)),
                            // Execute 40% of time price scrapper scenario
                            Choice.withWeight(40d, exec(UserJourneys.priceUpdate))


                    )

            );


  }

  private ScenarioBuilder scn = scenario("DemostoreApiSimulation")
          .exec(
                  exec(initSession),
                  exec(Categories.list),
                  pause(2)
                          .exec(Products.list),
                  pause(2)
                          .exec(Products.get).
                          pause(2)
                          .exec(Products.update),
                  pause(2)
                          .repeat(3).on(exec(Products.create)),
                  pause(2)
                          /*Create 3 products using repeat DSL block */
                          .exec(Categories.update)

          );

  {

    setUp(
            Scenarios.defaultScn.injectOpen(
                            rampUsers(USER_COUNT).during(RAMP_DURATION)),
            Scenarios.noAdminScn.injectOpen(rampUsers(5).during(Duration.ofSeconds(10))))
            .protocols(httpProtocol);


  }
}
