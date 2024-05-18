package gatlingdemostoreapi;

import io.gatling.javaapi.core.ChainBuilder;

import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.doIf;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.jmesPath;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class Authentication {
  public static final ChainBuilder authenticate =

          //run the authenticate code only if it's false i.e the user is not authenticated
          doIf(session -> !session.getBoolean("authenticated")).then(

                  exec(
                          http("Authenticate User")
                                  .post("/api/authenticate")
                                  //Can directly use the JSON within the transaction instead of RawFileBody
                                  .body(StringBody("{\n" +
                                          "    \"username\": \"admin\",\n" +
                                          "    \"password\": \"admin\"\n" +
                                          "}"))
                                  //Check if the response returned is 200
                                  .check(status().is(200))
                                  //Using jmesPath instead of jsonPath
                                  .check(jmesPath("token").saveAs("jwt")))
                          //Set the user as authenticated once the above code block is run
                          .exec(session -> session.set("authenticated", true))


          );


}