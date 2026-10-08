import org.example.Homework1;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

@Tag("homework2")
public class Homework2 {
    private static final int REPEATS = 10;
    private final Random random = new Random();

    @BeforeEach
    void setUp() {
        System.out.println("======================Test method start");
    }

    @AfterEach
    void tearDown() {
        System.out.println("Test method end\n======================");
    }

    @RepeatedTest(REPEATS)
    void testIsEven() {
        int n = random.nextInt(100) + 1;
        boolean expected = (n % 2 == 0);

        boolean actual = Homework1.isEven(n);

        assertEquals(expected, actual, "isEven(" + n + ")");
    }

    @RepeatedTest(REPEATS)
    void testSumToN() {
        int n = random.nextInt(10) + 1;
        int expected = n * (n + 1) / 2;

        int actual = Homework1.sumToN(n);

        assertEquals(expected, actual, "sumToN(" + n + ")");
    }

    @RepeatedTest(REPEATS)
    void testCalcAverage() {
        int a = random.nextInt(100);
        int b = random.nextInt(100);
        int c = random.nextInt(100);
        List<Integer> list = List.of(a, b, c);
        double expected = (a + b + c) / 3.0;

        double actual = Homework1.calcAverage(list);

        assertEquals(expected, actual, 0.0001, "calcAverage(" + list + ")");
    }

    @RepeatedTest(REPEATS)
    void testRemoveSpecificName() {
        List<String> list = List.of("Java", "Kotlin", "Python");
        String nameToRemove = list.get(random.nextInt(list.size()));
        List<String> expected = new ArrayList<>(list);
        expected.remove(nameToRemove);

        List<String> actual = Homework1.removeSpecificName(list, nameToRemove);

        assertIterableEquals(expected, actual,
                "removeSpecificName(" + list + ", \"" + nameToRemove + "\")");
    }

    @RepeatedTest(20)
    void testCheckAccess() {
        int age = random.nextInt(100);
        String expected = age > 18 ? "Allowed" : "Denied";

        String actual = Homework1.checkAccess(age);

        assertEquals(expected, actual, "checkAccess(" + age + ")");
    }

    @RepeatedTest(REPEATS)
    void testIsPositive() {
        int n = random.nextInt(200) - 100;
        boolean expected = n >= 0;

        boolean actual = Homework1.isPositive(n);

        assertEquals(expected, actual, "isPositive(" + n + ")");
    }

    @RepeatedTest(REPEATS)
    void testBlastOff() {
        int start = random.nextInt(5) + 1;
        StringBuilder expected = new StringBuilder();
        for (int i = start; i >= 1; i--) {
            expected.append(i).append(" ");
        }
        expected.append("Поехали!");

        String actual = Homework1.blastOff(start);

        assertEquals(expected.toString(), actual, "blastOff(" + start + ")");
    }

    @RepeatedTest(REPEATS)
    void testFindMax() {
        int[] arr = {random.nextInt(50), random.nextInt(50), random.nextInt(50)};
        int expected = Arrays.stream(arr).max().getAsInt();

        int actual = Homework1.findMax(arr);

        assertEquals(expected, actual, "findMax(" + Arrays.toString(arr) + ")");
    }

    @ParameterizedTest(name = "getGrade({0}) = {1}")
    @CsvSource({
            "0, E", "20, E",
            "21, D", "40, D",
            "41, C", "60, C",
            "61, B", "80, B",
            "81, A", "100, A",
            "-1, Error", "101, Error"
    })
    void testGetGrade(int score, String expected) {
        String actual = Homework1.getGrade(score);

        assertEquals(expected, actual, "getGrade(" + score + ")");
    }

    static Stream<Arguments> messagesProvider() {
        return Stream.of(
                Arguments.of(new String[]{"info", "Bug", "warn"}, true),
                Arguments.of(new String[]{"Bug"}, true),
                Arguments.of(new String[]{"bug", "ok"}, true),
                Arguments.of(new String[]{"ok", "BUG"}, true),
                Arguments.of(new String[]{"error", "warn", "bUg"}, true),
                Arguments.of(new String[]{"ok", "success", "done"}, false),
                Arguments.of(new String[]{"debug"}, false),
                Arguments.of(new String[]{"Bugs", "info"}, false),
                Arguments.of(new String[]{" Bug "}, false),
                Arguments.of(new String[]{}, false)
        );
    }

    @ParameterizedTest
    @MethodSource("messagesProvider")
    void testHasBug(String[] messages, boolean expected) {
        boolean actual = Homework1.hasBug(messages);

        assertEquals(expected, actual, "hasBug(" + Arrays.toString(messages) + ")");
    }

    @ParameterizedTest(name = "getEvenInRange({0}, {1}) = \"{2}\"")
    @CsvSource({
            "2, 6, '2 4 6'",
            "10, 15, '10 12 14'",
            "1, 1, ''",
            "2, 2, '2'",
            "1, 2, '2'",
            "3, 9, '4 6 8'",
            "0, 4, '0 2 4'",
            "-4, 0, '-4 -2 0'",
            "7, 8, '8'",
            "5, 3, ''"
    })
    void testGetEvenInRange(int start, int end, String expected) {
        String actual = Homework1.getEvenInRange(start, end);

        assertEquals(expected, actual, "getEvenInRange(" + start + ", " + end + ")");
    }

    static Stream<Arguments> stringArraysProvider() {
        Random random = new Random();
        return Stream.generate(() -> {
            int length = random.nextInt(5) + 1;
            String[] arr = new String[length];
            for (int i = 0; i < length; i++) {
                arr[i] = String.valueOf((char) ('A' + random.nextInt(26)));
            }
            return Arguments.of((Object) arr);
        }).limit(REPEATS);
    }

    @ParameterizedTest
    @MethodSource("stringArraysProvider")
    void testReverse(String[] arr) {
        List<String> reversed = new ArrayList<>(Arrays.asList(arr));
        Collections.reverse(reversed);
        String[] expected = reversed.toArray(new String[0]);

        String[] actual = Homework1.reverse(arr);

        assertArrayEquals(expected, actual, "reverse(" + Arrays.toString(arr) + ")");
    }
}
