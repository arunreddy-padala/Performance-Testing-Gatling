package gatlingdemostoreapi;

import java.util.List;
import java.util.Map;
import java.util.Random;

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

public class Products {

  //Feeder for Categories data
  public static final FeederBuilder.Batchable<String> productsFeeder =
          csv("data/products.csv").circular();

  public static ChainBuilder listAll =

          exec(
                  http("List all Products")
                          .get("/api/product")
                          .check(jmesPath("[*]").ofList().saveAs("allProducts"))

          );

  public static final ChainBuilder list =

          feed(productsFeeder)
                  .exec(
                          http("List Products")
                                  .get("/api/product?category=#{productCategoryId}")
                                  //Check if products with Category id 7 shouldn't appear
                                  .check(jsonPath("$[?(@.categoryId != \"#{productCategoryId}\")]").notExists())
                                  //Save the return list and use it in subsequent calls
                                  .check(jmesPath("[*].id").ofList().saveAs("allProductIds")));


  public static final ChainBuilder get =

          //Using session variables store the product is in a list and randomly get one
          exec(session -> {
            List<Integer> allProductIds = session.getList("allProductIds");
            return session.set("productId", allProductIds.get(new Random().nextInt(allProductIds.size())));
          })

                  .exec(
                          http("Get a Product")
                                  //Using the above randomly selected productId
                                  .get("/api/product/#{productId}")
                                  .check(jmesPath("id").ofInt().isEL("#{productId}"))
                                  //Capture the entire json of the get call and save as product
                                  .check(jmesPath("@").ofMap().saveAs("product")));





  public static final ChainBuilder update =

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


  public static final ChainBuilder create =


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