package pij.main;

    /**
     * This class is for validating board dimensions. This is especially useful for when making custom boards.
     */

public final class BoardValidator {
    private BoardValidator() {}

    /**
     * Validates the board dimensions.
     * <p>
     * Rules:
     * <ul>
     *   <li> Number of columns (m) must be between 7 and 26. </li>
     *   <li> Number of rows (n) must be between 10 and 99. </li>
     *   <li> Total number of cells (m * n) must be at least 192. </li>
     * </ul>
     *
     * @param m number of columns.
     * @param n number of rows.
     * @throws IllegalArgumentException if any rule is violated.
     */

    public static void validate(int m, int n) {
        if (m < 7 || m > 26) throw new IllegalArgumentException("M must be 7 to 26");
        if (n < 10 || n > 99) throw new IllegalArgumentException("N must be 10 to 99");
        if (m * n < 192) throw new IllegalArgumentException("M*N must be >= 192");
    }
}
