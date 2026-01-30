package pij.main;

public final class BoardValidator {
    private BoardValidator() {}

    public static void validate(int m, int n) {
        if (m < 7 || m > 26) throw new IllegalArgumentException("M must be 7 to 26");
        if (n < 10 || n > 99) throw new IllegalArgumentException("N must be 10 to 99");
        if (m * n < 192) throw new IllegalArgumentException("M*N must be >= 192");
    }
}
