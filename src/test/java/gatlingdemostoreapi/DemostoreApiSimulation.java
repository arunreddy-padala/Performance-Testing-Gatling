package gatlingdemostoreapi;

import java.time.Duration;
import java.util.*;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;
import io.gatling.javaapi.jdbc.*;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;
import static io.gatling.javaapi.jdbc.JdbcDsl.*;

public class DemostoreApiSimulation extends Simulation {

  private HttpProtocolBuilder httpProtocol = http
    .baseUrl("https://demostore.gatling.io")
    .inferHtmlResources()
    .acceptHeader("*/*")
    .acceptEncodingHeader("gzip, deflate, br")
    .userAgentHeader("PostmanRuntime/7.36.1");
  
  private Map<CharSequence, String> headers_0 = Map.ofEntries(
    Map.entry("Cache-Control", "no-cache"),
    Map.entry("Postman-Token", "8f3729dc-a63d-4279-96f8-2d6a3b480f14")
  );
  
  private Map<CharSequence, String> headers_1 = Map.ofEntries(
    Map.entry("Cache-Control", "no-cache"),
    Map.entry("Postman-Token", "8a9b7eb6-98ca-4f86-b4d2-da6edeecd781")
  );
  
  private Map<CharSequence, String> headers_2 = Map.ofEntries(
    Map.entry("Cache-Control", "no-cache"),
    Map.entry("Postman-Token", "d4270c62-0ea1-40e7-9a5f-4eb7292fca91")
  );
  
  private Map<CharSequence, String> headers_3 = Map.ofEntries(
    Map.entry("Cache-Control", "no-cache"),
    Map.entry("Content-Type", "application/json"),
    Map.entry("Postman-Token", "57959971-2494-4660-81e1-8dfb08ee1647")
  );
  
  private Map<CharSequence, String> headers_4 = Map.ofEntries(
    Map.entry("Cache-Control", "no-cache"),
    Map.entry("Content-Type", "application/json"),
    Map.entry("Postman-Token", "c2171275-ba07-4c14-b80d-e2cfcb53bf56"),
    Map.entry("authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTcxMzcxNTkwNywiZXhwIjoxNzEzNzE5NTA3fQ.C7ReyEi6vju-jrRya8X_ukjPly42vvkWIzV2IUAVHyE")
  );
  
  private Map<CharSequence, String> headers_5 = Map.ofEntries(
    Map.entry("Cache-Control", "no-cache"),
    Map.entry("Content-Type", "application/json"),
    Map.entry("Postman-Token", "79f16511-1316-473d-8e68-ed2e36745e85"),
    Map.entry("authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTcxMzcxNTkwNywiZXhwIjoxNzEzNzE5NTA3fQ.C7ReyEi6vju-jrRya8X_ukjPly42vvkWIzV2IUAVHyE")
  );
  
  private Map<CharSequence, String> headers_6 = Map.ofEntries(
    Map.entry("Cache-Control", "no-cache"),
    Map.entry("Content-Type", "application/json"),
    Map.entry("Postman-Token", "4fe753bd-1bb0-4359-95b0-c91964800016"),
    Map.entry("authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTcxMzcxNTkwNywiZXhwIjoxNzEzNzE5NTA3fQ.C7ReyEi6vju-jrRya8X_ukjPly42vvkWIzV2IUAVHyE")
  );
  
  private Map<CharSequence, String> headers_7 = Map.ofEntries(
    Map.entry("Cache-Control", "no-cache"),
    Map.entry("Content-Type", "application/json"),
    Map.entry("Postman-Token", "f559477a-84ad-4232-bd26-c6e08f991141"),
    Map.entry("authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTcxMzcxNTkwNywiZXhwIjoxNzEzNzE5NTA3fQ.C7ReyEi6vju-jrRya8X_ukjPly42vvkWIzV2IUAVHyE")
  );
  
  private Map<CharSequence, String> headers_8 = Map.ofEntries(
    Map.entry("Cache-Control", "no-cache"),
    Map.entry("Content-Type", "application/json"),
    Map.entry("Postman-Token", "c316311a-624e-49ce-9eb1-b5195b4a68d8"),
    Map.entry("authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTcxMzcxNTkwNywiZXhwIjoxNzEzNzE5NTA3fQ.C7ReyEi6vju-jrRya8X_ukjPly42vvkWIzV2IUAVHyE")
  );


  private ScenarioBuilder scn = scenario("DemostoreApiSimulation")
    .exec(
      http("request_0")
        .get("/api/category")
        .headers(headers_0),
      pause(15),
      http("request_1")
        .get("/api/product?category=7")
        .headers(headers_1),
      pause(14),
      http("request_2")
        .get("/api/product/34")
        .headers(headers_2),
      pause(14),
      http("request_3")
        .post("/api/authenticate")
        .headers(headers_3)
        .body(RawFileBody("gatlingdemostoreapi/demostoreapisimulation/0003_request.json")),
      pause(16),
      http("request_4")
        .put("/api/product/34")
        .headers(headers_4)
        .body(RawFileBody("gatlingdemostoreapi/demostoreapisimulation/0004_request.json")),
      pause(24),
      http("request_5")
        .post("/api/product")
        .headers(headers_5)
        .body(RawFileBody("gatlingdemostoreapi/demostoreapisimulation/0005_request.json")),
      pause(34),
      http("request_6")
        .post("/api/product")
        .headers(headers_6)
        .body(RawFileBody("gatlingdemostoreapi/demostoreapisimulation/0006_request.json")),
      pause(20),
      http("request_7")
        .post("/api/product")
        .headers(headers_7)
        .body(RawFileBody("gatlingdemostoreapi/demostoreapisimulation/0007_request.json")),
      pause(9),
      http("request_8")
        .put("/api/category/7")
        .headers(headers_8)
        .body(RawFileBody("gatlingdemostoreapi/demostoreapisimulation/0008_request.json"))
    );

  {
	  setUp(scn.injectOpen(atOnceUsers(1))).protocols(httpProtocol);
  }
}
