package gatlingdemostore.pageObjects;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.FeederBuilder;

import static io.gatling.javaapi.core.CoreDsl.css;
import static io.gatling.javaapi.core.CoreDsl.csv;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.feed;
import static io.gatling.javaapi.core.CoreDsl.jsonFile;
import static io.gatling.javaapi.core.CoreDsl.substring;
import static io.gatling.javaapi.http.HttpDsl.http;

public final class Catalog {

  public static class Category {

    public static final FeederBuilder<String> categoryFeeder =
            csv("/Users/arunkumarreddy/Documents/Projects/Project-Gatling/gatling-demostore/src/test/resources/data/categoryDetails.csv")
                    .random();

    public static final ChainBuilder view =
            feed(categoryFeeder)
                    .exec(
                            //Parameterize the category type that we want to get
                            http("Load Category Page - #{categoryName}")
                                    .get("/category/#{categorySlug}")
                                    //Check within the extracted page if the css selector CategoryName matches our parameter value
                                    .check(css("#CategoryName").isEL("#{categoryName}")));


  }

  public static class Product {

    //Using external csv datasource for parameter values
    private static final FeederBuilder<Object> JsonFeederProducts =
            jsonFile("/Users/arunkumarreddy/Documents/Projects/Project-Gatling/gatling-demostore/src/test/resources/data/productDetails.json")
                    .random();

    public static final ChainBuilder view =

            feed(JsonFeederProducts)
                    .exec(
                            http("Load product page - #{name}")
                                    .get("/product/#{slug}")
                                    .check(css("#ProductDescription").isEL("#{description}")));


    public static final ChainBuilder add =

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