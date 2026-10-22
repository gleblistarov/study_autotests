import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("ui")
public class Homework5 {

    // 1.1. Список названий товаров
    static final By PRODUCT_NAMES_CSS = By.cssSelector(".product-card h4");
    static final By PRODUCT_NAMES_XPATH = By.xpath("//div[@class='product-card']/h4");

    // 1.2. Список цен товаров
    static final By PRODUCT_PRICES_CSS = By.cssSelector(".product-card > h4 + div");
    static final By PRODUCT_PRICES_XPATH = By.xpath("//div[@class='product-card']/h4/following-sibling::div[1]");

    // 1.3. Название товара с ценой 25
    static final By NAME_WITH_PRICE_25_CSS = By.cssSelector(".product-card[data-price='25'] > h4");
    static final By NAME_WITH_PRICE_25_XPATH = By.xpath("//div[@class='product-card'][div[normalize-space()='25 ₽']]/h4");

    // 1.4. Цена товара с названием «Стакан»
    static final By GLASS_PRICE_CSS = By.cssSelector(".product-card[data-name='Стакан'] > h4 + div");
    static final By GLASS_PRICE_XPATH = By.xpath("//h4[text()='Стакан']/following-sibling::div[1]");

    // 1.5. Список карточек товаров в корзине
    static final By CART_ITEMS_CSS = By.cssSelector("#cart-items .cart-item");
    static final By CART_ITEMS_XPATH = By.xpath("//div[@id='cart-items']/div[@class='cart-item']");

    // 1.6. Кнопка корзины
    static final By CART_BUTTON_CSS = By.cssSelector("#open-cart-btn");
    static final By CART_BUTTON_XPATH = By.xpath("//button[@id='open-cart-btn']");

    private static final String BASE_URL = "http://localhost:8080";
    private static final String GLASS_NAME = "Стакан";
    private static final String GLASS_PRICE = "40";
    private static final String PRICE_25_NAME = "Кружка";

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeAll
    static void createGoods() {
        List<Map<String, Object>> goods = given()
                .baseUri(BASE_URL)
                .when()
                .get("/goods/list?size=100")
                .then()
                .statusCode(200)
                .extract().jsonPath().getList("goods");

        boolean hasGlass = goods.stream().anyMatch(g -> GLASS_NAME.equals(g.get("name")));
        boolean hasPrice25 = goods.stream().anyMatch(g -> ((Number) g.get("price")).doubleValue() == 25.0);

        if (!hasGlass) {
            addGood(GLASS_NAME, 40.0);
        }
        if (!hasPrice25) {
            addGood(PRICE_25_NAME, 25.0);
        }
    }

    @BeforeEach
    void openShop() {
        ChromeOptions options = new ChromeOptions();
        if ("true".equals(System.getenv("HEADLESS"))) {
            options.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage");
        }
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("product-card")));
    }

    @AfterEach
    void closeBrowser() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @DisplayName("1.1 Список названий товаров")
    void productNames() {
        List<String> css = texts(PRODUCT_NAMES_CSS);
        List<String> xpath = texts(PRODUCT_NAMES_XPATH);

        assertThat(css).as("Названия товаров (CSS)").contains(GLASS_NAME);
        assertThat(xpath).as("Названия товаров (XPath)").isEqualTo(css);
    }

    @Test
    @DisplayName("1.2 Список цен товаров")
    void productPrices() {
        List<String> css = texts(PRODUCT_PRICES_CSS);
        List<String> xpath = texts(PRODUCT_PRICES_XPATH);

        assertThat(css)
                .as("Цены товаров (CSS)")
                .hasSameSizeAs(texts(PRODUCT_NAMES_CSS))
                .allMatch(price -> price.endsWith("₽"));
        assertThat(xpath).as("Цены товаров (XPath)").isEqualTo(css);
    }

    @Test
    @DisplayName("1.3 Название товара с ценой 25")
    void nameOfGoodWithPrice25() {
        List<String> css = texts(NAME_WITH_PRICE_25_CSS);
        List<String> xpath = texts(NAME_WITH_PRICE_25_XPATH);

        assertThat(css).as("Товар с ценой 25 (CSS)").isNotEmpty();
        assertThat(xpath).as("Товар с ценой 25 (XPath)").isEqualTo(css);
    }

    @Test
    @DisplayName("1.4 Цена товара «Стакан»")
    void priceOfGlass() {
        assertThat(driver.findElement(GLASS_PRICE_CSS).getText())
                .as("Цена «Стакан» (CSS)")
                .isEqualTo(GLASS_PRICE + " ₽");
        assertThat(driver.findElement(GLASS_PRICE_XPATH).getText())
                .as("Цена «Стакан» (XPath)")
                .isEqualTo(GLASS_PRICE + " ₽");
    }

    @Test
    @DisplayName("1.5 Список карточек товаров в корзине")
    void cartItems() {
        List<WebElement> addButtons = driver.findElements(By.cssSelector("[data-action='add-to-cart']"));
        addButtons.get(0).click();
        addButtons.get(1).click();
        driver.findElement(CART_BUTTON_CSS).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("cartModal")));

        assertThat(driver.findElements(CART_ITEMS_CSS)).as("Карточки в корзине (CSS)").hasSize(2);
        assertThat(driver.findElements(CART_ITEMS_XPATH)).as("Карточки в корзине (XPath)").hasSize(2);
    }

    @Test
    @DisplayName("1.6 Кнопка корзины")
    void cartButton() {
        assertThat(driver.findElement(CART_BUTTON_CSS).getText()).as("Кнопка корзины (CSS)").contains("Корзина");
        assertThat(driver.findElement(CART_BUTTON_XPATH).getText()).as("Кнопка корзины (XPath)").contains("Корзина");
    }

    private List<String> texts(By locator) {
        return driver.findElements(locator).stream().map(WebElement::getText).toList();
    }

    private static void addGood(String name, double price) {
        given()
                .baseUri(BASE_URL)
                .auth().preemptive().basic("admin", "secret123")
                .contentType(ContentType.JSON)
                .body(Map.of("name", name, "price", price))
                .when()
                .post("/goods/add")
                .then()
                .statusCode(200);
    }
}