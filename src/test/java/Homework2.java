import org.example.Homework1;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

public class Homework2 {
    private final Random random = new Random();

    @BeforeEach
    void setUp() {
        System.out.println("======================Test method start");
    }

    @AfterEach
    void tearDown() {
        System.out.println("Test method end\n======================");
    }

    @Test
    void testIsEven() {
        int n = random.nextInt(100) + 1;
        boolean actual = Homework1.isEven(n);
        boolean expected = (n % 2 == 0);
        printResult(actual == expected);
    }

    @Test
    void testSumToN() {
        int n = random.nextInt(10) + 1;
        int actual = Homework1.sumToN(n);
        printResult(actual > 0);
    }

    @Test
    void testCalcAverage() {
        List<Integer> list = List.of(10, 20, 30);
        double actual = Homework1.calcAverage(list);
        printResult(actual == 20.0);
    }

    @Test
    void testRemoveSpecificName() {
        List<String> list = List.of("Java", "Kotlin", "Python");
        List<String> actual = Homework1.removeSpecificName(list, "Python");
        printResult(!actual.contains("Python"));
    }

    @RepeatedTest(20)
    void testCheckAccess() {
        int age = random.nextInt(100);
        String actual = Homework1.checkAccess(age);
        String expected = age > 18 ? "Allowed" : "Denied";
        printResult(actual.equals(expected));
    }

    @RepeatedTest(5)
    void testIsPositive() {
        int n = random.nextInt(200) - 100;
        boolean actual = Homework1.isPositive(n);
        boolean expected = n >= 0;
        printResult(actual == expected);
    }

    @RepeatedTest(5)
    void testBlastOff() {
        int start = random.nextInt(5) + 1;
        String actual = Homework1.blastOff(start);
        printResult(actual.endsWith("Поехали!"));
    }

    @RepeatedTest(5)
    void testFindMax() {
        int[] arr = {random.nextInt(50), random.nextInt(50), random.nextInt(50)};
        int actual = Homework1.findMax(arr);
        printResult(actual >= arr[0] && actual >= arr[1] && actual >= arr[2]);
    }

    static Stream<Integer> randomScoresProvider() {
        return Stream.generate(() -> new Random().nextInt(101)).limit(5);
    }

    @ParameterizedTest
    @MethodSource("randomScoresProvider")
    void testGetGrade(int score) {
        String actual = Homework1.getGrade(score);
        boolean isPassed = actual.equals("A") || actual.equals("B") || actual.equals("C") ||
                actual.equals("D") || actual.equals("E") || actual.equals("Error");
        printResult(isPassed);
    }

    static Stream<Arguments> randomMessagesProvider() {
        return Stream.of(
                Arguments.of((Object) new String[]{"info", "Bug", "warn"}),
                Arguments.of((Object) new String[]{"ok", "success", "done"})
        );
    }

    @ParameterizedTest
    @MethodSource("randomMessagesProvider")
    void testHasBug(String[] messages) {
        boolean actual = Homework1.hasBug(messages);
        boolean expected = Arrays.asList(messages).contains("Bug");
        printResult(actual == expected);
    }

    static Stream<Arguments> rangeProvider() {
        return Stream.of(
                Arguments.of(new int[]{2, 6}),
                Arguments.of(new int[]{10, 15})
        );
    }

    @ParameterizedTest
    @MethodSource("rangeProvider")
    void testGetEvenInRange(int[] range) {
        String actual = Homework1.getEvenInRange(range[0], range[1]);
        printResult(actual != null);
    }

    static Stream<Arguments> stringArraysProvider() {
        return Stream.of(
                Arguments.of((Object) new String[]{"A", "B", "C"}),
                Arguments.of((Object) new String[]{"X", "Y"})
        );
    }

    @ParameterizedTest
    @MethodSource("stringArraysProvider")
    void testReverse(String[] arr) {
        String[] actual = Homework1.reverse(arr);
        printResult(actual.length == arr.length && actual[0].equals(arr[arr.length - 1]));
    }

    private void printResult(boolean isSuccess) {
        if (isSuccess) {
            System.out.println("TEST PASSED");
        } else {
            System.out.println("TEST FAILED");
        }
    }
}
