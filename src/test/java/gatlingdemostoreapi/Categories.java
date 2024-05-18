package gatlingdemostoreapi;

import java.util.List;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.FeederBuilder;

import static gatlingdemostoreapi.Headers.authorizationHeader;
import static io.gatling.javaapi.core.CoreDsl.ElFileBody;
import static io.gatling.javaapi.core.CoreDsl.csv;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.feed;
import static io.gatling.javaapi.core.CoreDsl.jmesPath;
import static io.gatling.javaapi.core.CoreDsl.jsonPath;
import static io.gatling.javaapi.http.HttpDsl.http;

public class Categories {

  //Feeder for Categories data
  public static final FeederBuilder.Batchable<String> categoriesFeeder =
          csv("data/categories.csv").random();

  public static final ChainBuilder list =

          exec(
                  http("List Categories")
                          .get("/api/category")
                          //Return a array of strings
                          .check(jmesPath("[? id == `6`].name").ofList().is(List.of("For Her"))));


  public static final ChainBuilder update =

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