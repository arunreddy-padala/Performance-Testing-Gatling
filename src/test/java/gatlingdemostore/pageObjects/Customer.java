package gatlingdemostore.pageObjects;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.FeederBuilder;

import static io.gatling.javaapi.core.CoreDsl.csv;
import static io.gatling.javaapi.core.CoreDsl.feed;
import static io.gatling.javaapi.core.CoreDsl.substring;
import static io.gatling.javaapi.http.HttpDsl.http;

public final class Customer {

  private static final FeederBuilder<String> loginFeeder =
          csv("/Users/arunkumarreddy/Documents/Projects/Project-Gatling/gatling-demostore/src/test/resources/data/loginDetails.csv")
                  .circular();

  private static final String uri1 = "demostore.gatling.io";

  public static final ChainBuilder login =

          feed(loginFeeder)
                  .exec(
                          http("Load login page")
                                  .get("/login")
                                  //Css check to see if we are in the login page
                                  .check(substring("Username:")))

                  .exec(

                          session -> {
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
                  .exec(

                          session -> {
                            return session;

                          }

                  );


}
