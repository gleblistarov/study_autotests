import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.codeborne.selenide.CollectionCondition.size;
import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.value;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.$x;
import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.Selenide.webdriver;
import static com.codeborne.selenide.WebDriverConditions.url;
import static com.codeborne.selenide.WebDriverConditions.urlContaining;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("ui")
public class Homework6 {

    private static final String BASE_URL = "http://localhost:8080";
    private static final String LOGIN = "admin";
    private static final String PASSWORD = "secret123";
    private static final boolean HEADLESS = "true".equals(System.getenv("HEADLESS"));

    private final List<Long> createdIds = new ArrayList<>();
    private final List<String> createdNames = new ArrayList<>();

    // ЛЕКЦИЯ. Задача 1: Selenium

    @Nested
    @DisplayName("Задача 1. Selenium")
    class Task1Selenium {

        private WebDriver driver;
        private WebDriverWait wait;

        @BeforeEach
        void startBrowser() {
            ChromeOptions options = new ChromeOptions();
            if (HEADLESS) {
                options.addArguments("--headless=new");
            }
            driver = new ChromeDriver(options);
            wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        }

        @AfterEach
        void closeBrowser() {
            if (driver != null) {
                driver.quit();
            }
            deleteCreatedGoods();
        }

        @Test
        @DisplayName("1.1 Товар, добавленный через админку, отображается на витрине")
        void addedInAdminGoodIsShownInShop() {
            String name = uniqueName("Ваза");
            createdNames.add(name);

            loginToAdmin(LOGIN, PASSWORD);
            wait.until(ExpectedConditions.urlToBe(BASE_URL + "/admin"));

            driver.findElement(By.id("n-name")).sendKeys(name);
            driver.findElement(By.id("n-price")).sendKeys("120");
            driver.findElement(By.id("add-btn")).click();
            wait.until(ExpectedConditions.textToBePresentInElementLocated(
                    By.id("toast-container"), "Товар успешно добавлен!"));

            driver.findElement(By.linkText("Вернуться на сайт")).click();

            WebElement card = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//div[@class='product-card'][h4[text()='" + name + "']]")));
            assertThat(card.findElement(By.tagName("h4")).getText()).as("Название на витрине").isEqualTo(name);
            assertThat(card.getText()).as("Цена на витрине").contains("120 ₽");
        }

        @Test
        @DisplayName("1.2 Товар, добавленный в корзину, отображается в корзине")
        void addedToCartGoodIsShownInCart() {
            String name = uniqueName("Чайник");
            long id = createGood(name, 90);

            openShop();
            addToCart(id);
            driver.findElement(By.id("open-cart-btn")).click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("cartModal")));

            WebElement cartItem = driver.findElement(By.id("cart-item-" + id));
            assertThat(cartItem.isDisplayed()).as("Товар виден в корзине").isTrue();
            assertThat(cartItem.getText()).as("Карточка в корзине").contains(name, "90 ₽");
            assertThat(driver.findElement(By.id("cart-count")).getText()).as("Счётчик корзины").isEqualTo("1");
        }

        @Test
        @DisplayName("1.3 Вход в админку с неверным логином и паролем")
        void loginWithWrongCredentialsFails() {
            loginToAdmin("wrong-user", "wrong-password");

            wait.until(ExpectedConditions.urlContains("/login?error"));
            WebElement error = driver.findElement(By.cssSelector(".alert-danger"));
            assertThat(error.getText()).as("Сообщение об ошибке входа").isEqualTo("Неверные учетные данные пользователя");
            assertThat(driver.findElements(By.id("add-btn"))).as("Админка не открылась").isEmpty();
        }

        @Test
        @DisplayName("1.4 Корзина после обновления страницы (стенд её не сохраняет)")
        void cartAfterRefresh() {
            String name = uniqueName("Кружка");
            long id = createGood(name, 30);

            openShop();
            addToCart(id);
            assertThat(driver.findElement(By.id("cart-count")).getText()).as("Счётчик до обновления").isEqualTo("1");

            driver.navigate().refresh();
            wait.until(ExpectedConditions.presenceOfElementLocated(By.className("product-card")));

            assertThat(driver.findElement(By.id("cart-count")).getText())
                    .as("Счётчик корзины после обновления страницы")
                    .isEqualTo("0");
            driver.findElement(By.id("open-cart-btn")).click();
            assertThat(driver.findElements(By.cssSelector("#cart-items .cart-item")))
                    .as("Товары в корзине после обновления страницы")
                    .isEmpty();
        }

        private void openShop() {
            driver.get(BASE_URL);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.className("product-card")));
        }

        private void addToCart(long id) {
            driver.findElement(By.cssSelector("#card-" + id + " [data-action='add-to-cart']")).click();
        }

        private void loginToAdmin(String login, String password) {
            driver.get(BASE_URL + "/admin");
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
            driver.findElement(By.id("username")).sendKeys(login);
            driver.findElement(By.id("password")).sendKeys(password);
            driver.findElement(By.cssSelector("button[type='submit']")).click();
        }
    }

    // ЛЕКЦИЯ. Задача 2: Selenide

    @Nested
    @DisplayName("Задача 2. Selenide")
    class Task2Selenide {

        @BeforeEach
        void openBrowser() {
            configureSelenide();
            open("/");
        }

        @AfterEach
        void closeBrowser() {
            Selenide.closeWebDriver();
            deleteCreatedGoods();
        }

        @Test
        @DisplayName("2.1 Товар, добавленный через админку, отображается на витрине")
        void addedInAdminGoodIsShownInShop() {
            String name = uniqueName("Ваза");
            createdNames.add(name);

            loginToAdmin(LOGIN, PASSWORD);
            webdriver().shouldHave(url(BASE_URL + "/admin"));

            $("#n-name").setValue(name);
            $("#n-price").setValue("120");
            $("#add-btn").click();
            $("#toast-container").shouldHave(text("Товар успешно добавлен!"));

            $x("//a[text()='Вернуться на сайт']").click();

            SelenideElement card = $x("//div[@class='product-card'][h4[text()='" + name + "']]");
            card.shouldBe(visible);
            card.$("h4").shouldHave(exactText(name));
            card.shouldHave(text("120 ₽"));
        }

        @Test
        @DisplayName("2.2 Товар, добавленный в корзину, отображается в корзине")
        void addedToCartGoodIsShownInCart() {
            String name = uniqueName("Чайник");
            long id = createGood(name, 90);

            openShop();
            addToCart(id);
            $("#open-cart-btn").click();
            $("#cartModal").shouldBe(visible);

            $("#cart-item-" + id).shouldBe(visible).shouldHave(text(name), text("90 ₽"));
            $("#cart-count").shouldHave(exactText("1"));
        }

        @Test
        @DisplayName("2.3 Вход в админку с неверным логином и паролем")
        void loginWithWrongCredentialsFails() {
            loginToAdmin("wrong-user", "wrong-password");

            webdriver().shouldHave(urlContaining("/login?error"));
            $(".alert-danger").shouldBe(visible).shouldHave(exactText("Неверные учетные данные пользователя"));
            $("#add-btn").shouldNotBe(visible);
        }

        @Test
        @DisplayName("2.4 Корзина после обновления страницы (стенд её не сохраняет)")
        void cartAfterRefresh() {
            String name = uniqueName("Кружка");
            long id = createGood(name, 30);

            openShop();
            addToCart(id);
            $("#cart-count").shouldHave(exactText("1"));

            Selenide.refresh();
            $(".product-card").shouldBe(visible);

            $("#cart-count").shouldHave(exactText("0"));
            $("#open-cart-btn").click();
            $$("#cart-items .cart-item").shouldHave(size(0));
        }

        @Test
        @DisplayName("2.5 Заказ дороже 300 ₽ показывает JS Alert")
        void orderOver300ShowsAlert() {
            String name = uniqueName("Сервиз");
            long id = createGood(name, 150);

            openShop();
            increaseQty(id);
            increaseQty(id);
            $("#q-" + id).shouldHave(value("3"));
            addToCart(id);

            $("#open-cart-btn").click();
            $("#total-price").shouldHave(exactText("450"));
            $("#makeOrder").click();

            Selenide.confirm("[SmartShop]: Денег не хватает! Сумма 450 ₽ превышает лимит 300 ₽.");
        }
    }

    // ВЕБИНАР. Задача 3: Selenide

    @Nested
    @DisplayName("Задача 3. Вебинар")
    class Task3Webinar {

        @BeforeEach
        void setUp() {
            configureSelenide();
        }

        @AfterEach
        void cleanUp() {
            Selenide.closeWebDriver();
            deleteCreatedGoods();
        }

        @Test
        @DisplayName("3.1 Оплата трёх единиц товара до 300 ₽ показывает уведомление о заказе")
        void orderThreeItemsShowsNotification() {
            long id = createGood(uniqueName("Блокнот"), 50);

            openShop();
            increaseQty(id);
            increaseQty(id);
            $("#q-" + id).shouldHave(value("3"));
            addToCart(id);

            $("#open-cart-btn").click();
            $("#total-price").shouldHave(exactText("150"));
            $("#makeOrder").click();

            $$(".toast").findBy(text("Заказ принят в обработку!")).shouldBe(visible);
            $("#main-title").shouldHave(exactText("Заказ успешно оформлен!"));
            $("#cart-count").shouldHave(exactText("0"));
        }

        @Test
        @DisplayName("3.2 Общая цена в корзине для нескольких разных товаров")
        void cartTotalForDifferentGoods() {
            long pen = createGood(uniqueName("Ручка"), 40);
            long pencil = createGood(uniqueName("Карандаш"), 25);
            long eraser = createGood(uniqueName("Ластик"), 15);

            openShop();
            addToCart(pen);
            increaseQty(pencil);
            addToCart(pencil);
            addToCart(eraser);

            $("#open-cart-btn").click();
            $("#cart-item-" + pen).shouldHave(text("40 ₽"));
            $("#cart-item-" + pencil).shouldHave(text("50 ₽"));
            $("#cart-item-" + eraser).shouldHave(text("15 ₽"));
            $("#total-price").shouldHave(exactText("105"));
            $("#cart-count").shouldHave(exactText("4"));
        }

        @Test
        @DisplayName("3.3 Уведомление после добавления товара в админке")
        void addGoodInAdminShowsNotification() {
            String name = uniqueName("Лампа");
            createdNames.add(name);

            loginToAdmin(LOGIN, PASSWORD);
            webdriver().shouldHave(url(BASE_URL + "/admin"));
            $("#n-name").setValue(name);
            $("#n-price").setValue("75");
            $("#add-btn").click();

            $(".toast").shouldBe(visible).shouldHave(exactText("Товар успешно добавлен!"));
            $x("//tbody[@id='tbody']//input[@value='" + name + "']").shouldBe(visible);
        }

        @Test
        @DisplayName("3.4 Изменения товара из админки видны в списке товаров")
        void editedInAdminGoodIsUpdatedInShop() {
            long id = createGood(uniqueName("Стул"), 100);
            String newName = uniqueName("Кресло");

            loginToAdmin(LOGIN, PASSWORD);
            webdriver().shouldHave(url(BASE_URL + "/admin"));
            $("#nm-" + id).shouldBe(visible).setValue(newName);
            $("#pr-" + id).setValue("140");
            $("button[data-action='update'][data-id='" + id + "']").click();
            $$(".toast").findBy(text("Товар #" + id + " обновлен")).shouldBe(visible);

            $x("//a[text()='Вернуться на сайт']").click();

            $("#card-" + id + " h4").shouldHave(exactText(newName));
            $("#card-" + id).shouldHave(text("140 ₽"));
        }
    }

    private static void configureSelenide() {
        Configuration.baseUrl = BASE_URL;
        Configuration.browser = "chrome";
        Configuration.headless = HEADLESS;
    }

    private static void openShop() {
        open("/");
        $(".product-card").shouldBe(visible);
    }

    private static void increaseQty(long id) {
        $("#card-" + id + " [data-action='qty-change'][data-step='1']").click();
    }

    private static void addToCart(long id) {
        $("#card-" + id + " [data-action='add-to-cart']").click();
    }

    private static void loginToAdmin(String login, String password) {
        open("/admin");
        $("#username").shouldBe(visible).setValue(login);
        $("#password").setValue(password);
        $("button[type='submit']").click();
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

    private void deleteCreatedGoods() {
        createdIds.forEach(id -> adminApi().delete("/goods/{id}", id));

        if (!createdNames.isEmpty()) {
            List<Map<String, Object>> goods = given()
                    .baseUri(BASE_URL)
                    .queryParam("size", 100)
                .when()
                    .get("/goods/list")
                .then()
                    .statusCode(200)
                    .extract().jsonPath().getList("goods");

            goods.stream()
                    .filter(good -> createdNames.contains(good.get("name")))
                    .forEach(good -> adminApi().delete("/goods/{id}", ((Number) good.get("id")).longValue()));
        }

        createdIds.clear();
        createdNames.clear();
    }
}
