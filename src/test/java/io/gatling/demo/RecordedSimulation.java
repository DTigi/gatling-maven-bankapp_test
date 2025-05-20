package io.gatling.demo;

import java.time.Duration;
import java.util.*;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;
import io.gatling.javaapi.jdbc.*;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;
import static io.gatling.javaapi.jdbc.JdbcDsl.*;

public class RecordedSimulation extends Simulation {

  private HttpProtocolBuilder httpProtocol = http
    .baseUrl("http://localhost:8080")
    .inferHtmlResources()
    .acceptHeader("*/*")
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3")
    .doNotTrackHeader("1")
    .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:138.0) Gecko/20100101 Firefox/138.0");
  
  private Map<CharSequence, String> headers_0 = Map.ofEntries(
    Map.entry("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"),
    Map.entry("Priority", "u=0, i"),
    Map.entry("Upgrade-Insecure-Requests", "1")
  );
  
  private Map<CharSequence, String> headers_1 = Map.ofEntries(
    Map.entry("Accept", "text/css,*/*;q=0.1"),
    Map.entry("Priority", "u=2")
  );
  
  private Map<CharSequence, String> headers_6 = Map.ofEntries(
    Map.entry("Accept", "image/avif,image/webp,image/png,image/svg+xml,image/*;q=0.8,*/*;q=0.5"),
    Map.entry("Priority", "u=6")
  );
  
  private Map<CharSequence, String> headers_7 = Map.of("Priority", "u=4");
  
  private Map<CharSequence, String> headers_8 = Map.ofEntries(
    Map.entry("Accept", "application/json,*/*"),
    Map.entry("Priority", "u=4")
  );
  
  private Map<CharSequence, String> headers_9 = Map.of("Priority", "u=0");
  
  private Map<CharSequence, String> headers_19 = Map.ofEntries(
    Map.entry("Origin", "http://localhost:8081"),
    Map.entry("Priority", "u=0")
  );
  
  private Map<CharSequence, String> headers_20 = Map.ofEntries(
    Map.entry("Origin", "http://localhost:8080"),
    Map.entry("Priority", "u=0")
  );
  
  private String uri1 = "localhost";
  // кормушки
  private static final FeederBuilder <String> userDataFeeder = csv("/Users/elenatiginanu/dtigi/JavaProjects/standartmock/test_accounts.csv").circular();

  private ScenarioBuilder scn = scenario("RecordedSimulation")
    .feed(userDataFeeder)

    .exec(
      // Hello,
//      http("request_0")
//        .get("/swagger-ui/index.html")
//        .headers(headers_0)
//        .resources(
//          http("request_1")
//            .get("/swagger-ui/swagger-ui.css")
//            .headers(headers_1),
//          http("request_2")
//            .get("/swagger-ui/index.css")
//            .headers(headers_1),
//          http("request_3")
//            .get("/swagger-ui/swagger-ui-standalone-preset.js"),
//          http("request_4")
//            .get("/swagger-ui/swagger-ui-bundle.js"),
//          http("request_5")
//            .get("/swagger-ui/swagger-initializer.js"),
//          http("request_6")
//            .get("/swagger-ui/favicon-32x32.png")
//            .headers(headers_6),
//          http("request_7")
//            .get("/v3/api-docs/swagger-config")
//            .headers(headers_7),
//          http("request_8")
//            .get("/v3/api-docs")
//            .headers(headers_8)
//        ),
      pause(2),
      // say_hello,
      http("hello")
        .get("/hello?name=#{username}")
        .headers(headers_9)
        .check(substring("Привет, #{username}!"))
        .check(bodyString().saveAs("hello_responseBody")),
//      pause(2),
      // login,
//      http("request_10")
//        .get("http://" + uri1 + ":8081/swagger-ui/index.html")
//        .headers(headers_0)
//        .resources(
//          http("request_11")
//            .get("http://" + uri1 + ":8081/swagger-ui/swagger-ui.css")
//            .headers(headers_1),
//          http("request_12")
//            .get("http://" + uri1 + ":8081/swagger-ui/swagger-ui-standalone-preset.js"),
//          http("request_13")
//            .get("http://" + uri1 + ":8081/swagger-ui/swagger-initializer.js"),
//          http("request_14")
//            .get("http://" + uri1 + ":8081/swagger-ui/index.css")
//            .headers(headers_1),
//          http("request_15")
//            .get("http://" + uri1 + ":8081/swagger-ui/swagger-ui-bundle.js"),
//          http("request_16")
//            .get("http://" + uri1 + ":8081/swagger-ui/favicon-32x32.png")
//            .headers(headers_6),
//          http("request_17")
//            .get("http://" + uri1 + ":8081/v3/api-docs/swagger-config")
//            .headers(headers_7),
//          http("request_18")
//            .get("http://" + uri1 + ":8081/v3/api-docs")
//            .headers(headers_8)
//        ),
      pause(2),
      // LOGIN,
      http("login")
        .post("http://" + uri1 + ":8081/auth/login?username=#{username}&password=#{password}")
        .headers(headers_19)
        .check(substring("✅ Успешный вход: #{username}"))
        .check(bodyString().saveAs("login_responseBody")),
      pause(2),
      // select-recepient,
      http("select-recipient")
        .post("/transactions/select-recipient?username=user10&accountNumber=8d02762b001c")
        .headers(headers_20)
        .check(substring("✅ Получатель выбран: Eustolia Reilly (Счет: 8d02762b001c)"))
        .check(bodyString().saveAs("recipient_responseBody")),
      pause(2),
      // transfer,
      http("transfer")
        .post("/transactions/transfer?amount=400")
        .headers(headers_20)
        .check(substring("✅ Перевод завершен! 400.0₽ переведено на счет 8d02762b001c"))
        .check(bodyString().saveAs("transfer_responseBody")),
      pause(2),
      // logout,
      http("request_22")
        .post("http://" + uri1 + ":8081/auth/logout")
        .headers(headers_19)
        .check(substring("✅ Успешный выход"))
        .check(bodyString().saveAs("logout_responseBody"))
    )

  // Логи
        .exec(session -> {
    System.out.println(// "Search params: " +
//            "depart=" + session.getString("departCity") +
//            ", arrive=" + session.getString("arriveCity") + "\n" +
//            "Current outboundFlight: " + session.getString("outboundFlight") + "\n" +
            "hello_responseBody:" + session.getString("hello_responseBody")+ "\n" +
            "login_responseBody:" + session.getString("login_responseBody")+ "\n" +
            "recipient_responseBody:" + session.getString("recipient_responseBody")+ "\n" +
            "transfer_responseBody:" + session.getString("transfer_responseBody")+ "\n" +
            "logout_responseBody:" + session.getString("logout_responseBody")) ;
    return session;
  });

  {
	  setUp(scn.injectOpen(atOnceUsers(1))).protocols(httpProtocol);
  }
}
