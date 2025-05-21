package io.gatling.demo;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.demo.ChainBuilders.*;

public class TestFindMax extends Simulation {

  private HttpProtocolBuilder httpProtocol = http
          .baseUrl("http://localhost:8080")
          .inferHtmlResources()
          .acceptHeader("*/*")
          .acceptEncodingHeader("gzip, deflate")
          .acceptLanguageHeader("ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3")
          .doNotTrackHeader("1")
          .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:138.0) Gecko/20100101 Firefox/138.0");

  private ScenarioBuilder scnTransfer = scenario("UC1_Transfer")
          .forever().on(
                  pace(20)
                          .exec(DataPrepare)
                          .pause(2)
                          .exec(login)
                          .pause(2)
                          .exec(select_recipient)
                          .pause(2)
                          .exec(transfer)
                          .pause(2)
                          .exec(logout)
          );

  private ScenarioBuilder scnLoginLogout = scenario("UC2_LoginLogout")
          .forever().on(
                  pace(20)
                          .exec(DataPrepare)
                          .pause(2)
                          .exec(login)
                          .pause(2)
                          .exec(logout)
          );

  private ScenarioBuilder scnRegistration = scenario("UC3_Registration")
          .forever().on(
                  pace(20)
                          .exec(DataPrepare)
                          .pause(2)
                          .exec(register)
          );

  {
      setUp(scnTransfer.injectClosed(
                      rampConcurrentUsers(0).to(2).during(10),
                      constantConcurrentUsers(2).during(1200),
                      rampConcurrentUsers(2).to(4).during(10),
                      constantConcurrentUsers(4).during(1200),
                      rampConcurrentUsers(4).to(6).during(10),
                      constantConcurrentUsers(6).during(1200),
                      rampConcurrentUsers(6).to(8).during(10),
                      constantConcurrentUsers(8).during(1200)),

              scnLoginLogout.injectClosed(
                      rampConcurrentUsers(0).to(1).during(10),
                      constantConcurrentUsers(1).during(1200),
                      rampConcurrentUsers(1).to(2).during(10),
                      constantConcurrentUsers(2).during(1200),
                      rampConcurrentUsers(2).to(3).during(10),
                      constantConcurrentUsers(3).during(1200),
                      rampConcurrentUsers(3).to(4).during(10),
                      constantConcurrentUsers(4).during(1200)),

              scnRegistration.injectClosed(
                      rampConcurrentUsers(0).to(2).during(10),
                      constantConcurrentUsers(2).during(1200),
                      rampConcurrentUsers(2).to(4).during(10),
                      constantConcurrentUsers(4).during(1200),
                      rampConcurrentUsers(4).to(6).during(10),
                      constantConcurrentUsers(6).during(1200),
                      rampConcurrentUsers(6).to(8).during(10),
                      constantConcurrentUsers(8).during(1200))
      ).protocols(httpProtocol).maxDuration(4850);
  }
}