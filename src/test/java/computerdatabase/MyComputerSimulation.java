package computerdatabase;


import java.util.Map;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.FeederBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class MyComputerSimulation extends Simulation {

  private HttpProtocolBuilder httpProtocol = http
          .baseUrl("https://computer-database.gatling.io")
          .inferHtmlResources(AllowList(), DenyList(".*\\.js", ".*\\.css", ".*\\.gif", ".*\\.jpeg", ".*\\.jpg", ".*\\.ico", ".*\\.woff", ".*\\.woff2", ".*\\.(t|o)tf", ".*\\.png", ".*\\.svg", ".*detectportal\\.firefox\\.com.*"))
          .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
          .acceptEncodingHeader("gzip, deflate, br")
          .acceptLanguageHeader("en-GB,en-US;q=0.9,en;q=0.8,te;q=0.7")
          .upgradeInsecureRequestsHeader("1")
          .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36");

  FeederBuilder.Batchable searchFeeder = csv("data/search.csv").random();
  //Circular - Loop through each record in csv file at return to start when there are no records
  FeederBuilder.Batchable computerFeeder = csv("data/computer.csv").circular();

  private ScenarioBuilder scn = scenario("MyComputerSimulation")
          .exec(
                  http("LoadHomePage")
                          .get("/computers"),
                  pause(17)
                          .feed(searchFeeder),
                  //SEARCH FOR COMPUTER
                  http("SearchComputers_#{searchCriterion}")
                          //Search criterion will be randomly selected from the csv file
                          .get("/computers?f=#{searchCriterion}")
                          //CSS check to capture the computer name matches with what we have defined in CSV file
                          .check(css("a:contains('#{searchComputerName}')","href").saveAs("computerURL")),
                  pause(2),
                  //LOAD SPECIFIC COMPUTER
                  //Using searchComputerName to remove hardcoded references
                  http("LoadComputerDetails_#{searchComputerName}")
                          .get("computerURL"),
                  pause(13),
                  //OPEN HOME PAGE
                  http("LoadHomePage")
                          .get("/computers"),
                  pause(2),
                  //VIEW NEXT COMPUTERS
                  http("ViewNextComputers")
                          .get("/computers?p=1"),
                  pause(2),
                  http("ViewNextComputers")
                          .get("/computers?p=2"),
                  pause(7),
                  http("ViewNextComputers")
                          .get("/computers/new"),
                  pause(22)
                          .feed(computerFeeder),
                  //CREATED NEW COMPUTER
                  http("CreateNewComputer_#{computerName}")
                          .post("/computers")
                          .formParam("name", "#{computerName}")
                          .formParam("introduced", "#{introduced}")
                          .formParam("discontinued", "#{discontinued}")
                          .formParam("company", "#{companyId}")
                          //Check if the response is valid
                          .check(status().is(200))
          );

  {
    setUp(
            scn.injectOpen(nothingFor(5),
                    atOnceUsers(1),
                    //Ramp up the users to 5 for a duration of 10 seconds
                    rampUsers(5).during(10),
                    //Add 2 constant users for 20 seconds duration
                    constantUsersPerSec(2).during(20))).protocols(httpProtocol);
  }
}
