package gatlingdemostore.pageObjects;

import io.gatling.javaapi.core.ChainBuilder;

import static io.gatling.javaapi.core.CoreDsl.css;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.regex;
import static io.gatling.javaapi.core.CoreDsl.substring;
import static io.gatling.javaapi.http.HttpDsl.http;

public final class CmsPages {

  public static final ChainBuilder homePage =
          exec(http("Load Home Page")
                  .get("/")
                  //Check to make sure home page loads correctly
                  .check(regex("<title>Gatling Demo-Store</title>").exists())
                  //Using css check to capture the csrf value and to be used in request 6
                  .check(css("#_csrf", "content").saveAs("csrfValue")));


  public static final ChainBuilder aboutUs =

          exec(http("Load About Us")
                  .get("/about-us")
                  //Check to ensure the About Us title can be retrieved from the page
                  .check(substring("About Us")));

}
