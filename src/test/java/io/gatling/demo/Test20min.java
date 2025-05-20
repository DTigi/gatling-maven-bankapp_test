package io.gatling.demo;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.FeederBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;

public class Test20min extends Simulation {

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

    ChainBuilder DataPrepare = exec(
    feed(userDataFeeder)
    .feed(senderDataFeeder)

    // генерируем необходимые параметры
    .exec(session -> {
      // случайные параметры
      String reg_fullName = "User" + UUID.randomUUID().toString().substring(0, 8);
      String reg_phone = String.format("%09d", ThreadLocalRandom.current().nextInt(100000000, 999999999));
      String reg_username = "user" + ThreadLocalRandom.current().nextInt(1000, 9999);
      String reg_password = "pass" + ThreadLocalRandom.current().nextInt(1000, 9999);
      Integer amount = ThreadLocalRandom.current().nextInt(1, 500);

      // Записываем в сессию
      return session
              .set("reg_fullName", reg_fullName)
              .set("reg_phone", reg_phone)
              .set("reg_username", reg_username)
              .set("reg_password", reg_password)
              .set("amount", amount);
                        })
    );

    ChainBuilder hello = exec(
      http("hello")
        .get("/hello?name=#{username}")
        .headers(headers_9)
        .check(substring("Привет, #{username}!"))
        .check(bodyString().saveAs("hello_responseBody"))
    );

    ChainBuilder register = exec(
      http("register")
        .post("/auth/register?fullName=#{reg_fullName}&phone=#{reg_phone}&username=#{reg_username}&password=#{reg_password}")
        .headers(headers_20)
        .check(substring("#{reg_password}"))
        .check(bodyString().saveAs("register_responseBody"))
    );

    ChainBuilder login = exec(
      http("login")
        .post("/auth/login?username=#{sender_username}&password=#{sender_password}")
        .headers(headers_20)
        .check(substring("✅ Успешный вход: #{sender_username}"))
        .check(bodyString().saveAs("login_responseBody"))
    );

    ChainBuilder select_recipient = exec(
      http("select-recipient")
        .post("/transactions/select-recipient?username=#{username}&accountNumber=#{AccountNumber}")
        .headers(headers_20)
        .check(substring("✅ Получатель выбран: #{fullName} (Счет: #{AccountNumber})"))
        .check(bodyString().saveAs("recipient_responseBody"))
        .check(regex("Баланс отправителя: (.*)").saveAs("sender_balance"))
        .check(regex("Счет: ([^)]*)").saveAs("recipient_account_number"))
    );

    ChainBuilder transfer = exec(
      http("transfer")
        .post("/transactions/transfer?amount=#{amount}")
        .headers(headers_20)
        .check(bodyString().saveAs("transfer_responseBody"))
        .check(regex("✅ Перевод завершен! #{amount}.+?₽ переведено на счет #{recipient_account_number}"))
    );

    ChainBuilder logout = exec(
      http("logout")
        .post("/auth/logout")
        .headers(headers_20)
        .check(substring("✅ Успешный выход"))
        .check(bodyString().saveAs("logout_responseBody"))
    );

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
      setUp(scnTransfer.injectClosed(//rampConcurrentUsers(0).to(2).during(10),
                      constantConcurrentUsers(2).during(1200)),
              scnLoginLogout.injectClosed(//rampConcurrentUsers(0).to(1).during(10),
                      constantConcurrentUsers(1).during(1200)),
              scnRegistration.injectClosed(//rampConcurrentUsers(0).to(2).during(10),
                      constantConcurrentUsers(2).during(1200))
      ).protocols(httpProtocol).maxDuration(1200);
  }
}