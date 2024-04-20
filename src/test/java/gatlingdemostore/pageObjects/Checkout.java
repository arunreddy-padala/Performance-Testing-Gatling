package gatlingdemostore.pageObjects;

import gatlingdemostore.DemoStoreSimulation;
import io.gatling.javaapi.core.ChainBuilder;

import static io.gatling.javaapi.core.CoreDsl.css;
import static io.gatling.javaapi.core.CoreDsl.doIf;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.substring;
import static io.gatling.javaapi.http.HttpDsl.http;

public final class Checkout {

  private static final String uri1 = "demostore.gatling.io";

  public static final ChainBuilder ViewCart =
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

                            return session;

                          }

                  );


  public static final ChainBuilder CompleteCheckoutView =

          exec(
                  http("Checkout Cart")
                          .get("http://" + uri1 + "/cart/checkout")
                          .check(substring("Thanks for your order! See you soon!")));

}