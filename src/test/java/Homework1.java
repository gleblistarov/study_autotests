import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Homework1 {

    public static void main(String[] args) {
        System.out.println(isEven(2));
        System.out.println(checkAccess(17));
        System.out.println(isPositive(2));
        System.out.println(getGrade(90));
        System.out.println(blastOff(5));
        System.out.println(sumToN(5));
        System.out.println(hasBug(new String[] {"yes", "debug"}));
        System.out.println(getEvenInRange(2,9));
        System.out.println(findMax(new int[] {4, 8, 15, 16, 23, 42}));
        System.out.println(Arrays.toString(reverse(new String[]{"шалаш", "дед"})));
        System.out.println(calcAverage(List.of(1,2,3,4,5)));
        System.out.println(removeSpecificName(List.of("Саша","Маша","Даша"),"Даша"));
    }

    // Задача 1
    public static boolean isEven(int n) {
        return n % 2 == 0;
    }

    // Задача 2
    public static String checkAccess(int age) {
        return age > 18 ? "Allowed" : "Denied";
    }

    // Задача 3
    public static boolean isPositive(int n) {
        return n >= 0 ? true : false;
    }

    // Задача 4
    public static String getGrade(int score) {
        if (score >= 0 && score <= 20) return "E";
        if (score >= 21 && score <= 40) return "D";
        if (score >= 41 && score <= 60) return "C";
        if (score >= 61 && score <= 80) return "B";
        if (score >= 81 && score <= 100) return "A";
        return "Error";
    }

    // Задача 5
    public static String blastOff(int start) {
        StringBuilder result = new StringBuilder();
        for (int i = start; i >= 1; i--) {
            result.append(i).append(" ");
        }
        result.append("Поехали!");
        return result.toString();
    }

    // Задача 6
    public static int sumToN(int n) {
        int sum = 0;
        for (int i = 1; i <= n; i++) {
            sum += i;
        }
        return sum;
    }

    // Задача 7
    public static boolean hasBug(String[] messages) {
        for (String msg : messages) {
            if ("Bug".equalsIgnoreCase(msg)) {
                return true;
            }
        }
        return false;
    }

    // Задача 8
    public static String getEvenInRange(int start, int end) {
        StringBuilder result = new StringBuilder();
        for (int i = start; i <= end; i++) {
            if (i % 2 == 0) {
                if (result.length() > 0) {
                    result.append(" ");
                }
                result.append(i);
            }
        }
        return result.toString();
    }

    // Задача 9
    public static int findMax(int[] arr) {
        if (arr.length == 0) {
            throw new IllegalArgumentException("Массив пуст");
        }
        int max = arr[0];
        for (int i = 1; i < arr.length; i++) {
            if (arr[i] > max) {
                max = arr[i];
            }
        }
        return max;
    }

    // Задача 10
    public static String[] reverse(String[] arr) {
        String[] reversed = new String[arr.length];
        for (int i = 0; i < arr.length; i++) {
            reversed[i] = arr[arr.length - 1 - i];
        }
        return reversed;
    }

    // Задача 11
    public static double calcAverage(List<Integer> list) {
        if (list == null || list.isEmpty()) {
            return 0;
        }
        double sum = 0;
        for (int number : list) {
            sum += number;
        }
        return sum / list.size();
    }

    // Задача 12
    public static List<String> removeSpecificName(List<String> list, String nameToRemove) {
        List<String> result = new ArrayList<>();
        for (String name : list) {
            if (!name.equals(nameToRemove)) {
                result.add(name);
            }
        }
        return result;
    }
}