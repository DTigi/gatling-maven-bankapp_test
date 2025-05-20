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
  
  private Map<CharSequence, String> headers_9 = Map.of("Priority", "u=0");
  
  private Map<CharSequence, String> headers_20 = Map.ofEntries(
    Map.entry("Origin", "http://localhost:8080"),
    Map.entry("Priority", "u=0")
  );

  // кормушки
  private static final FeederBuilder <String> userDataFeeder = csv("/home/dtigi/projects/BankApp/test_accounts.csv").circular();
  private static final FeederBuilder <String> senderDataFeeder = csv("fake_users.csv").circular();

  private ScenarioBuilder scn = scenario("RecordedSimulation")
    .feed(userDataFeeder)
    .feed(senderDataFeeder)

    .exec(
      pause(2),
      // say_hello,
      http("hello")
        .get("/hello?name=#{username}")
        .headers(headers_9)
        .check(substring("Привет, #{username}!"))
        .check(bodyString().saveAs("hello_responseBody")),

      pause(2),
      // login,
      http("login")
        .post("/auth/login?username=#{sender_username}&password=#{sender_password}")
        .headers(headers_20)
        .check(substring("✅ Успешный вход: #{sender_username}"))
        .check(bodyString().saveAs("login_responseBody")),

      pause(2),
      // select-recipient,
      http("select-recipient")
        .post("/transactions/select-recipient?username=#{username}&accountNumber=#{AccountNumber}")
        .headers(headers_20)
        .check(substring("✅ Получатель выбран: #{fullName} (Счет: #{AccountNumber})"))
        .check(bodyString().saveAs("recipient_responseBody"))
        .check(regex("Баланс отправителя: (.*)").saveAs("sender_balance"))
        .check(regex("Счет: ([^)]*)").saveAs("recipient_account_number")),

      pause(2),
      // transfer,
      http("transfer")
        .post("/transactions/transfer?amount=#{sender_balance}")
        .headers(headers_20)
        .check(bodyString().saveAs("transfer_responseBody"))
        .check(regex("✅ Перевод завершен! #{sender_balance}₽ переведено на счет #{recipient_account_number}")),

      pause(2),
      // logout,
      http("logout")
        .post("/auth/logout")
        .headers(headers_20)
        .check(substring("✅ Успешный выход"))
        .check(bodyString().saveAs("logout_responseBody"))
    )

    // Логи
    .exec(session -> {
    System.out.println("hello_responseBody:" + session.getString("hello_responseBody")+ "\n" +
            "login_responseBody:" + session.getString("login_responseBody")+ "\n" +
            "recipient_responseBody:" + session.getString("recipient_responseBody")+ "\n" +
            "Selected recipient account: " + session.getString("recipient_account_number") + "\n" +
            "transfer_responseBody:" + session.getString("transfer_responseBody")+ "\n" +
            "logout_responseBody:" + session.getString("logout_responseBody")) ;
    return session;
    });

  {
	  setUp(scn.injectOpen(atOnceUsers(20))).protocols(httpProtocol);
  }
}