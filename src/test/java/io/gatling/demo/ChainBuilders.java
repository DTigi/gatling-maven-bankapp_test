package io.gatling.demo;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.FeederBuilder;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;

public class ChainBuilders {
    private static final Map<CharSequence, String> headers_9 = Map.of("Priority", "u=0");

    private static final Map<CharSequence, String> headers_20 = Map.ofEntries(
            Map.entry("Origin", "http://localhost:8080"),
            Map.entry("Priority", "u=0")
    );

    private static final FeederBuilder<String> userDataFeeder = csv("/home/dtigi/projects/BankApp/test_accounts.csv").circular();
    private static final FeederBuilder<String> senderDataFeeder = csv("fake_users.csv").circular();

    public static final ChainBuilder DataPrepare = exec(
            feed(userDataFeeder)
                    .feed(senderDataFeeder)
                    .exec(session -> {
                        String reg_fullName = "User" + UUID.randomUUID().toString().substring(0, 8);
                        String reg_phone = String.format("%09d", ThreadLocalRandom.current().nextInt(100000000, 999999999));
                        String reg_username = "user" + ThreadLocalRandom.current().nextInt(1000, 9999);
                        String reg_password = "pass" + ThreadLocalRandom.current().nextInt(1000, 9999);
                        Integer amount = ThreadLocalRandom.current().nextInt(1, 50);

                        return session
                                .set("reg_fullName", reg_fullName)
                                .set("reg_phone", reg_phone)
                                .set("reg_username", reg_username)
                                .set("reg_password", reg_password)
                                .set("amount", amount);
                    })
    );

    public static final ChainBuilder hello = exec(
            http("hello")
                    .get("/hello?name=#{username}")
                    .headers(headers_9)
                    .check(substring("Привет, #{username}!"))
                    .check(bodyString().saveAs("hello_responseBody"))
    );

    public static final ChainBuilder register = exec(
            http("register")
                    .post("/auth/register?fullName=#{reg_fullName}&phone=#{reg_phone}&username=#{reg_username}&password=#{reg_password}")
                    .headers(headers_20)
                    .check(substring("#{reg_password}"))
                    .check(bodyString().saveAs("register_responseBody"))
    );

    public static final ChainBuilder login = exec(
            http("login")
                    .post("/auth/login?username=#{sender_username}&password=#{sender_password}")
                    .headers(headers_20)
                    .check(substring("✅ Успешный вход: #{sender_username}"))
                    .check(bodyString().saveAs("login_responseBody"))
    );

    public static final ChainBuilder select_recipient = exec(
            http("select-recipient")
                    .post("/transactions/select-recipient?username=#{username}&accountNumber=#{AccountNumber}")
                    .headers(headers_20)
                    .check(substring("✅ Получатель выбран: #{fullName} (Счет: #{AccountNumber})"))
                    .check(bodyString().saveAs("recipient_responseBody"))
                    .check(regex("Баланс отправителя: (.*)").saveAs("sender_balance"))
                    .check(regex("Счет: ([^)]*)").saveAs("recipient_account_number"))
    );

    public static final ChainBuilder transfer = exec(
            http("transfer")
                    .post("/transactions/transfer?amount=#{amount}")
                    .headers(headers_20)
                    .check(bodyString().saveAs("transfer_responseBody"))
                    .check(regex("✅ Перевод завершен! #{amount}.+?₽ переведено на счет #{recipient_account_number}"))
    );

    public static final ChainBuilder logout = exec(
            http("logout")
                    .post("/auth/logout")
                    .headers(headers_20)
                    .check(substring("✅ Успешный выход"))
                    .check(bodyString().saveAs("logout_responseBody"))
    );
}
