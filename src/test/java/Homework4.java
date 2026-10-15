import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.JsonConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.path.json.config.JsonPathConfig.NumberReturnType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.withArgs;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;

@Tag("api")
public class Homework4 {

    private static final String BASE_URI = "http://localhost:8080";
    private static final String LOGIN = "admin";
    private static final String PASSWORD = "secret123";
    private static final long NON_EXISTENT_ID = 0L;
    private static final long MAX_SAFE_ID = 49L;
    private static RequestSpecification spec;
    private static RequestSpecification authSpec;

    public record Good(Long id, String name, Double price) {
    }

    @BeforeAll
    static void setUpSpecifications() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        RestAssured.config = RestAssuredConfig.config()
                .jsonConfig(JsonConfig.jsonConfig().numberReturnType(NumberReturnType.DOUBLE));

        spec = new RequestSpecBuilder()
                .setBaseUri(BASE_URI)
                .setBasePath("/goods")
                .setContentType(ContentType.JSON)
                .build();

        authSpec = new RequestSpecBuilder()
                .addRequestSpecification(spec)
                .setAuth(RestAssured.preemptive().basic(LOGIN, PASSWORD))
                .build();
    }

    @BeforeEach
    void deleteAllGoods() {
        List<Long> ids = given(spec)
                .queryParam("size", 100)
                .when()
                .get("/list")
                .then()
                .statusCode(200)
                .extract().jsonPath().getList("goods.id", Long.class);

        ids.forEach(id -> given(authSpec).delete("/{id}", id));
    }

    @Test
    @DisplayName("1.1 given/when/then: код 200 и пустой список")
    void emptyListWithGivenWhenThen() {
        given()
                .baseUri(BASE_URI)
                .when()
                .get("/goods/list")
                .then()
                .statusCode(200)
                .body("goods", empty());
    }

    @Test
    @DisplayName("1.2 RequestSpecification: код 200 и пустой список")
    void emptyListWithRequestSpecification() {
        RequestSpecification requestSpec = new RequestSpecBuilder()
                .setBaseUri(BASE_URI)
                .setBasePath("/goods")
                .build();

        given(requestSpec)
                .when()
                .get("/list")
                .then()
                .statusCode(200)
                .body("goods", empty());
    }

    @Test
    @DisplayName("1.3 Созданный товар есть в списке: проверки REST Assured")
    void createdGoodIsInListRestAssured() {
        String name = uniqueName("Laptop");
        long id = createGood(name, 999.9);

        given(spec)
                .when()
                .get("/list")
                .then()
                .statusCode(200)
                .body("goods.id", hasItem((int) id))
                .body("goods.find { it.id == %s }.name", withArgs(id), equalTo(name))
                .body("goods.find { it.id == %s }.price", withArgs(id), equalTo(999.9));
    }

    @Test
    @DisplayName("1.4 Созданный товар есть в списке: проверки AssertJ")
    void createdGoodIsInListAssertJ() {
        String name = uniqueName("Phone");
        long id = createGood(name, 500.0);

        List<Good> goods = given(spec)
                .when()
                .get("/list")
                .then()
                .statusCode(200)
                .extract().jsonPath().getList("goods", Good.class);

        assertThat(goods)
                .as("GET /goods/list должен вернуть созданный товар")
                .contains(new Good(id, name, 500.0));
    }

    @Test
    @DisplayName("GET /goods/list 200: возвращает все товары")
    void listReturnsAllGoods() {
        String first = uniqueName("Mouse");
        String second = uniqueName("Keyboard");
        long firstId = createGood(first, 10.0);
        long secondId = createGood(second, 20.5);

        List<Good> goods = given(spec)
                .when()
                .get("/list")
                .then()
                .statusCode(200)
                .extract().jsonPath().getList("goods", Good.class);

        assertThat(goods)
                .as("Список товаров")
                .containsExactlyInAnyOrder(
                        new Good(firstId, first, 10.0),
                        new Good(secondId, second, 20.5));
    }

    @Test
    @DisplayName("GET /goods/list 200: пагинация page/size")
    void listReturnsPageOfGoods() {
        String first = uniqueName("Cable");
        String second = uniqueName("Charger");
        createGood(first, 1.0);
        createGood(second, 2.0);

        given(spec)
                .queryParam("page", 0)
                .queryParam("size", 1)
                .when()
                .get("/list")
                .then()
                .statusCode(200)
                .body("goods", hasSize(1))
                .body("goods[0].name", equalTo(first));

        given(spec)
                .queryParam("page", 1)
                .queryParam("size", 1)
                .when()
                .get("/list")
                .then()
                .statusCode(200)
                .body("goods", hasSize(1))
                .body("goods[0].name", equalTo(second));
    }

    @Test
    @DisplayName("GET /goods/list 400: отрицательный номер страницы")
    void listNegativePageReturns400() {
        String body = given(spec)
                .queryParam("page", -1)
                .when()
                .get("/list")
                .then()
                .statusCode(400)
                .extract().asString();

        assertThat(body).isEqualTo("Page index must not be less than zero");
    }

    @Test
    @DisplayName("GET /goods/list 400: размер страницы 0")
    void listZeroSizeReturns400() {
        String body = given(spec)
                .queryParam("size", 0)
                .when()
                .get("/list")
                .then()
                .statusCode(400)
                .extract().asString();

        assertThat(body).isEqualTo("Page size must not be less than one");
    }

    @Test
    @DisplayName("POST /goods/add 200: товар создан и доступен по id")
    void addGoodReturns200() {
        String name = uniqueName("Monitor");

        int id = given(authSpec)
                .body(goodBody(name, 150.0))
                .when()
                .post("/add")
                .then()
                .statusCode(200)
                .body("message", equalTo("success"))
                .body("data.id", greaterThan(0))
                .extract().path("data.id");

        given(authSpec)
                .when()
                .get("/{id}", id)
                .then()
                .statusCode(200)
                .body("name", equalTo(name))
                .body("price", equalTo(150.0));
    }

    @Test
    @DisplayName("POST /goods/add 400: товар с таким именем уже есть")
    void addDuplicateNameReturns400() {
        String name = uniqueName("Tablet");
        createGood(name, 300.0);

        given(authSpec)
                .body(goodBody(name, 300.0))
                .when()
                .post("/add")
                .then()
                .statusCode(400)
                .body("data", nullValue())
                .body("message", equalTo("Good with name '" + name + "' already exists!"));
    }

    @ParameterizedTest(name = "POST /goods/add 400: пустое имя, name = \"{0}\"")
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void addEmptyNameReturns400(String name) {
        given(authSpec)
                .body(goodBody(name, 10.0))
                .when()
                .post("/add")
                .then()
                .statusCode(400)
                .body("name", equalTo("Name cannot be empty"));
    }

    @Test
    @DisplayName("POST /goods/add 400: отрицательная цена")
    void addNegativePriceReturns400() {
        given(authSpec)
                .body(goodBody(uniqueName("Speaker"), -1.0))
                .when()
                .post("/add")
                .then()
                .statusCode(400)
                .body("price", equalTo("Price can not be less, than 0"));
    }

    @Test
    @DisplayName("POST /goods/add 401: запрос без авторизации")
    void addWithoutAuthReturns401() {
        given(spec)
                .body(goodBody(uniqueName("Camera"), 10.0))
                .when()
                .post("/add")
                .then()
                .statusCode(401);
    }

    @Test
    @DisplayName("POST /goods/add 401: неверный пароль")
    void addWrongPasswordReturns401() {
        given(spec)
                .auth().preemptive().basic(LOGIN, "wrong")
                .body(goodBody(uniqueName("Camera"), 10.0))
                .when()
                .post("/add")
                .then()
                .statusCode(401);
    }

    @Test
    @DisplayName("GET /goods/{id} 200: возвращает товар по id")
    void getGoodReturns200() {
        String name = uniqueName("Headphones");
        long id = createGood(name, 75.5);

        Good good = given(authSpec)
                .when()
                .get("/{id}", id)
                .then()
                .statusCode(200)
                .extract().as(Good.class);

        assertThat(good).isEqualTo(new Good(id, name, 75.5));
    }

    @Test
    @DisplayName("GET /goods/{id} 404: товара с таким id нет")
    void getNotExistingGoodReturns404() {
        given(authSpec)
                .when()
                .get("/{id}", NON_EXISTENT_ID)
                .then()
                .statusCode(404)
                .body("data", nullValue())
                .body("message", equalTo("Good with id '" + NON_EXISTENT_ID + "' is not found!"));
    }

    @Test
    @DisplayName("GET /goods/{id} 400: id не число")
    void getNotNumericIdReturns400() {
        String body = given(authSpec)
                .when()
                .get("/{id}", "abc")
                .then()
                .statusCode(400)
                .extract().asString();

        assertThat(body).contains("Failed to convert value", "\"abc\"");
    }

    @Test
    @DisplayName("GET /goods/{id} 401: запрос без авторизации (в Swagger не указан)")
    void getWithoutAuthReturns401() {
        given(spec)
                .when()
                .get("/{id}", 1)
                .then()
                .statusCode(401);
    }

    @Test
    @DisplayName("PATCH /goods/{id} 200: меняет имя и цену")
    void patchNameAndPriceReturns200() {
        long id = createGood(uniqueName("Watch"), 100.0);
        String newName = uniqueName("SmartWatch");

        Good patched = given(authSpec)
                .body(goodBody(newName, 120.0))
                .when()
                .patch("/{id}", id)
                .then()
                .statusCode(200)
                .extract().as(Good.class);

        assertThat(patched).isEqualTo(new Good(id, newName, 120.0));

        given(authSpec)
                .when()
                .get("/{id}", id)
                .then()
                .statusCode(200)
                .body("name", equalTo(newName))
                .body("price", equalTo(120.0));
    }

    @Test
    @DisplayName("PATCH /goods/{id} 200: без цены в запросе цена не меняется")
    void patchWithoutPriceKeepsPrice() {
        long id = createGood(uniqueName("Router"), 80.0);
        String newName = uniqueName("WifiRouter");

        given(authSpec)
                .body(goodBody(newName, null))
                .when()
                .patch("/{id}", id)
                .then()
                .statusCode(200)
                .body("name", equalTo(newName))
                .body("price", equalTo(80.0));
    }

    @Test
    @DisplayName("PATCH /goods/{id} 400: имя занято другим товаром")
    void patchDuplicateNameReturns400() {
        String takenName = uniqueName("Printer");
        createGood(takenName, 200.0);
        long id = createGood(uniqueName("Scanner"), 150.0);

        String body = given(authSpec)
                .body(goodBody(takenName, 150.0))
                .when()
                .patch("/{id}", id)
                .then()
                .statusCode(400)
                .extract().asString();

        assertThat(body).isEqualTo("Good with name '" + takenName + "' already exists!");
    }

    @Test
    @DisplayName("PATCH /goods/{id} 400: пустое имя")
    void patchEmptyNameReturns400() {
        given(authSpec)
                .body(goodBody("", 10.0))
                .when()
                .patch("/{id}", NON_EXISTENT_ID)
                .then()
                .statusCode(400)
                .body("name", equalTo("Name cannot be empty"));
    }

    @Test
    @DisplayName("PATCH /goods/{id} 400: отрицательная цена")
    void patchNegativePriceReturns400() {
        given(authSpec)
                .body(goodBody(uniqueName("Drone"), -5.0))
                .when()
                .patch("/{id}", NON_EXISTENT_ID)
                .then()
                .statusCode(400)
                .body("price", equalTo("Price can not be less, than 0"));
    }

    @Test
    @DisplayName("PATCH /goods/{id} 404: товара с таким id нет")
    void patchNotExistingGoodReturns404() {
        String body = given(authSpec)
                .body(goodBody(uniqueName("Ghost"), 1.0))
                .when()
                .patch("/{id}", NON_EXISTENT_ID)
                .then()
                .statusCode(404)
                .extract().asString();

        assertThat(body).isEqualTo("Good with id '" + NON_EXISTENT_ID + "' is not found");
    }

    @Test
    @DisplayName("PATCH /goods/{id} 401: запрос без авторизации")
    void patchWithoutAuthReturns401() {
        given(spec)
                .body(goodBody(uniqueName("Ghost"), 1.0))
                .when()
                .patch("/{id}", 1)
                .then()
                .statusCode(401);
    }

    @Test
    @DisplayName("DELETE /goods/{id} 200: товар удалён и пропал из списка")
    void deleteGoodReturns200() {
        long id = createGood(uniqueName("Lamp"), 25.0);

        String body = given(authSpec)
                .when()
                .delete("/{id}", id)
                .then()
                .statusCode(200)
                .extract().asString();

        assertThat(body).isEqualTo("Good with id '" + id + "' has been deleted successfully!");

        given(spec)
                .when()
                .get("/list")
                .then()
                .statusCode(200)
                .body("goods", empty());
    }

    @Test
    @DisplayName("DELETE /goods/{id} 404: товара с таким id нет")
    void deleteNotExistingGoodReturns404() {
        String body = given(authSpec)
                .when()
                .delete("/{id}", NON_EXISTENT_ID)
                .then()
                .statusCode(404)
                .extract().asString();

        assertThat(body).isEqualTo("Good with id '" + NON_EXISTENT_ID + "' is not found");
    }

    @Test
    @DisplayName("DELETE /goods/{id} 401: запрос без авторизации")
    void deleteWithoutAuthReturns401() {
        given(spec)
                .when()
                .delete("/{id}", 1)
                .then()
                .statusCode(401);
    }

    private static String uniqueName(String prefix) {
        return prefix + System.nanoTime();
    }

    private static Map<String, Object> goodBody(String name, Double price) {
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("price", price);
        return body;
    }

    private static long createGood(String name, double price) {
        long id = given(authSpec)
                .body(goodBody(name, price))
                .when()
                .post("/add")
                .then()
                .statusCode(200)
                .extract().jsonPath().getLong("data.id");

        assertThat(id)
                .as("id нового товара = %d. Перезапусти стенд с чистой базой: останови его, удали папку data и запусти снова", id)
                .isLessThanOrEqualTo(MAX_SAFE_ID);
        return id;
    }
}
