import org.example.Calculator;
import org.example.Homework1;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

@Tag("homework3")
public class Homework3 {

    // Метод возвращает булево значение
    @Test
    void testIsEvenReturnsBoolean() {
        assertAll(
                () -> assertEquals(true, Homework1.isEven(4), "isEven(4)"),
                () -> assertEquals(false, Homework1.isEven(7), "isEven(7)"),
                () -> assertEquals(true, Homework1.isEven(0), "isEven(0)")
        );
    }

    // Метод возвращает список
    @Test
    void testRemoveSpecificNameReturnsList() {
        List<String> source = List.of("Java", "Kotlin", "Python");
        List<String> expected = List.of("Java", "Kotlin");

        List<String> actual = Homework1.removeSpecificName(source, "Python");

        assertIterableEquals(expected, actual, "removeSpecificName(" + source + ", \"Python\")");
    }

    @Test
    void testCalcAverage() {
        List<Integer> source = List.of(10, 20, 30);

        double actual = Homework1.calcAverage(source);

        assertEquals(20.0, actual, 0.0001, "calcAverage(" + source + ")");
    }

    // Метод возвращает неверное значение, ассерт должен упасть
    @Test
    @Tag("failing")
    void testMultiplyReturnsWrongValue() {
        int actual = Calculator.multiply(3, 4);

        assertEquals(12, actual, "multiply(3, 4)");
    }
}