package com.example.fitlife_sumyatnoe.utils;

import org.junit.Test;
import static org.junit.Assert.*;

public class ValidationTest {

    @Test
    public void testEmailValidation_ValidEmail_ReturnsTrue() {
        String validEmail = "user@example.com";
        boolean isValid = validEmail.contains("@") && validEmail.contains(".");
        assertTrue("Valid email should pass", isValid);
    }

    @Test
    public void testEmailValidation_InvalidEmail_ReturnsFalse() {
        String invalidEmail = "userexample.com";
        boolean isValid = invalidEmail.contains("@") && invalidEmail.contains(".");
        assertFalse("Invalid email should fail", isValid);
    }

    @Test
    public void testPasswordValidation_ValidPassword_ReturnsTrue() {
        String password = "password123";
        boolean isValid = password.length() >= 6;
        assertTrue("Password with 6+ chars should be valid", isValid);
    }

    @Test
    public void testPasswordValidation_ShortPassword_ReturnsFalse() {
        String password = "123";
        boolean isValid = password.length() >= 6;
        assertFalse("Short password should be invalid", isValid);
    }
}