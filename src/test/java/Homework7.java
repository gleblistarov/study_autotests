import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.DragAndDropOptions;
import com.codeborne.selenide.Selenide;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.codeborne.selenide.CollectionCondition.size;
import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.open;
import static io.restassured.RestAssured.given;

@Tag("ui")
public class Homework7 {

    private static final String BASE_URL = "http://localhost:8080";
    private static final String LOGIN = "admin";
    private static final String PASSWORD = "secret123";

    private final List<Long> createdIds = new ArrayList<>();

    @BeforeEach
    void openBrowser() {
        Configuration.baseUrl = BASE_URL;
        Configuration.browser = "chrome";
        Configuration.headless = "true".equals(System.getenv("HEADLESS"));
    }

    @AfterEach
    void closeBrowser() {
        Selenide.closeWebDriver();
        createdIds.forEach(id -> adminApi().delete("/goods/{id}", id));
        createdIds.clear();
    }

    @Test
    @DisplayName("1.1 Перетащить товар в корзину с помощью Drag-and-Drop")
    void dragGoodToCart() {
        String name = uniqueName("Глобус");
        long id = createGood(name, 80);

        openShop();
        $("#cart-count").shouldHave(exactText("0"));

        dragToCart(id);

        $("#cart-count").shouldHave(exactText("1"));
        $(".toast").shouldHave(text(name + " (1 шт.) добавлен в корзину"));

        $("#open-cart-btn").click();
        $("#cartModal").shouldBe(visible);
        $("#cart-item-" + id).shouldBe(visible).shouldHave(text(name), text("80 ₽"));
        $("#total-price").shouldHave(exactText("80"));
    }

    @Test
    @DisplayName("1.2 Удалить добавленный товар из корзины: он больше не отображается")
    void removeGoodFromCart() {
        String name = uniqueName("Компас");
        long id = createGood(name, 60);

        openShop();
        dragToCart(id);
        $("#cart-count").shouldHave(exactText("1"));

        $("#open-cart-btn").click();
        $("#cart-item-" + id).shouldBe(visible);

        $("#cart-item-" + id + " [data-action='remove']").click();

        $("#cart-item-" + id).shouldNot(exist);
        $$("#cart-items .cart-item").shouldHave(size(0));
        $("#total-price").shouldHave(exactText("0"));
        $("#cart-count").shouldHave(exactText("0"));
    }

    private void openShop() {
        open("/");
        $(".product-card").shouldBe(visible);
    }

    private void dragToCart(long id) {
        $("#card-" + id).dragAndDrop(DragAndDropOptions.to("#open-cart-btn").usingJS());
    }

    private static RequestSpecification adminApi() {
        return given()
                .baseUri(BASE_URL)
                .auth().preemptive().basic(LOGIN, PASSWORD)
                .contentType(ContentType.JSON);
    }

    private static String uniqueName(String prefix) {
        return prefix + " " + (System.nanoTime() % 1_000_000);
    }

    private long createGood(String name, int price) {
        long id = adminApi()
                .body(Map.of("name", name, "price", price))
            .when()
                .post("/goods/add")
            .then()
                .statusCode(200)
                .extract().jsonPath().getLong("data.id");
        createdIds.add(id);
        return id;
    }
}
