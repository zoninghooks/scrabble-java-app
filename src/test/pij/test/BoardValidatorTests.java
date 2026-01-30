package test;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import pij.main.BoardValidator;

public class BoardValidatorTests {
    
    @Test //does it accept 192 cells? it should
    void boardValidator_acceptsValidDimensions() {
        assertDoesNotThrow(() -> BoardValidator.validate(16, 12)); 
    }

    @Test // does it accept columns under 7? it shouldn't
    void boardValidator_rejectsMTooSmall() {
        assertThrows(IllegalArgumentException.class, () -> BoardValidator.validate(6, 32));
    }

    @Test // does it accept columns over 26? it shouldn't
    void boardValidator_rejectsMTooLarge() {
        assertThrows(IllegalArgumentException.class, () -> BoardValidator.validate(27, 10));
    }

    @Test // does it acccept rows below 10? it shouldn't
    void boardValidator_rejectsNTooSmall() {
        assertThrows(IllegalArgumentException.class, () -> BoardValidator.validate(16, 9));
    }

    @Test // does it accept 100 cells? it shouldn't
    void boardValidator_rejectsAreaTooSmall() {
        assertThrows(IllegalArgumentException.class, () -> BoardValidator.validate(10, 10)); 
    }
}
