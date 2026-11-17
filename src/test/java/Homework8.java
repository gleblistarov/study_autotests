import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.value;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.Selenide.webdriver;
import static com.codeborne.selenide.WebDriverConditions.url;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@Tag("ui")
public class Homework8 {

    private static RequestSpecification adminApi;
    private static long startGoodId;

    @BeforeAll
    static void setUp() {
        TestConfig.print();

        if (TestConfig.isDebug()) {
            RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
        } else {
            RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        }

        Configuration.baseUrl = TestConfig.BASE_URL;
        Configuration.timeout = TestConfig.TIMEOUT_MS;
        Configuration.browser = "chrome";
        Configuration.headless = "true".equals(System.getenv("HEADLESS"));

        adminApi = new RequestSpecBuilder()
                .setBaseUri(TestConfig.API_URL)
                .setAuth(RestAssured.preemptive().basic(TestConfig.ADMIN_LOGIN, TestConfig.ADMIN_PASSWORD))
                .setContentType(ContentType.JSON)
                .build();

        deleteGoodsByName(TestConfig.START_GOOD_NAME);
        startGoodId = given(adminApi)
                .body(Map.of("name", TestConfig.START_GOOD_NAME, "price", TestConfig.START_GOOD_PRICE))
            .when()
                .post("/add")
            .then()
                .statusCode(200)
                .extract().jsonPath().getLong("data.id");
    }

    @AfterEach
    void closeBrowser() {
        Selenide.closeWebDriver();
    }

    @AfterAll
    static void tearDown() {
        given(adminApi).delete("/{id}", startGoodId);
        RestAssured.reset();
    }

    @Test
    @DisplayName("Стартовый товар из конфига доступен через API")
    void startGoodIsAvailableViaApi() {
        given(adminApi)
            .when()
                .get("/{id}", startGoodId)
            .then()
                .statusCode(200)
                .body("name", equalTo(TestConfig.START_GOOD_NAME))
                .body("price", equalTo((float) TestConfig.START_GOOD_PRICE));
    }

    @Test
    @DisplayName("Стартовый товар из конфига отображается на витрине")
    void startGoodIsShownInShop() {
        open("/");

        $("#card-" + startGoodId).shouldBe(visible);
        $("#card-" + startGoodId + " h4").shouldHave(exactText(TestConfig.START_GOOD_NAME));
        $("#card-" + startGoodId).shouldHave(text(TestConfig.START_GOOD_PRICE + " ₽"));
    }

    @Test
    @DisplayName("Вход в админку с credentials из конфига: стартовый товар есть в таблице")
    void loginToAdminWithConfigCredentials() {
        open("/admin");
        $("#username").shouldBe(visible).setValue(TestConfig.ADMIN_LOGIN);
        $("#password").setValue(TestConfig.ADMIN_PASSWORD);
        $("button[type='submit']").click();

        webdriver().shouldHave(url(TestConfig.BASE_URL + "/admin"));
        $("#nm-" + startGoodId).shouldHave(value(TestConfig.START_GOOD_NAME));
        $("#pr-" + startGoodId).shouldHave(value(String.valueOf(TestConfig.START_GOOD_PRICE)));
    }

    private static void deleteGoodsByName(String name) {
        List<Map<String, Object>> goods = given()
                .baseUri(TestConfig.API_URL)
                .queryParam("size", 100)
            .when()
                .get("/list")
            .then()
                .statusCode(200)
                .extract().jsonPath().getList("goods");

        goods.stream()
                .filter(good -> name.equals(good.get("name")))
                .forEach(good -> given(adminApi).delete("/{id}", ((Number) good.get("id")).longValue()));
    }
}
