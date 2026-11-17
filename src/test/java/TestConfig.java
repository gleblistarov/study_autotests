import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public final class TestConfig {

    private static final String FILE = "config.properties";
    private static final Properties PROPERTIES = load();

    public static final String BASE_URL = get("base.url");
    public static final String API_URL = get("api.url");
    public static final long TIMEOUT_MS = Long.parseLong(get("timeout.ms"));
    public static final String LOG_LEVEL = get("log.level");
    public static final String ADMIN_LOGIN = get("admin.login");
    public static final String ADMIN_PASSWORD = get("admin.password");
    public static final String START_GOOD_NAME = get("start.good.name");
    public static final int START_GOOD_PRICE = Integer.parseInt(get("start.good.price"));

    private TestConfig() {
    }

    private static Properties load() {
        Properties properties = new Properties();
        try (InputStream stream = TestConfig.class.getClassLoader().getResourceAsStream(FILE)) {
            if (stream == null) {
                throw new IllegalStateException("Не найден " + FILE + " в src/test/resources");
            }
            properties.load(new InputStreamReader(stream, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось прочитать " + FILE, e);
        }
        return properties;
    }

    private static String get(String key) {
        String value = PROPERTIES.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("В " + FILE + " не задан параметр " + key);
        }
        return value.trim();
    }

    public static boolean isDebug() {
        return "DEBUG".equalsIgnoreCase(LOG_LEVEL);
    }

    public static void print() {
        System.out.println("========== Параметры запуска (" + FILE + ") ==========");
        System.out.println("URL стенда:             " + BASE_URL);
        System.out.println("URL API:                " + API_URL);
        System.out.println("Тайм-аут элементов:     " + TIMEOUT_MS + " мс");
        System.out.println("Режим логирования:      " + LOG_LEVEL);
        System.out.println("Стартовый товар:        " + START_GOOD_NAME);
        System.out.println("Цена стартового товара: " + START_GOOD_PRICE + " ₽");
        System.out.println("====================================================");
    }
}
