package com.olaleyeone.auth.validator;

import com.olaleyeone.auth.integration.etc.PhoneNumberService;
import com.olaleyeone.auth.test.ComponentTest;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class ValidPhoneNumberValidatorTest extends ComponentTest {

    @InjectMocks
    private ValidPhoneNumberValidator validator;

    @Mock
    private PhoneNumberService phoneNumberService;

    @Test
    void nullShouldBeValid() {
        assertTrue(validator.isValid(null, null));
    }

    @Test
    void emptyStringShouldBeValid() {
        assertTrue(validator.isValid("", null));
    }

    @Test
    void shouldBeValid() {
        String phoneNumber = "123";
        Mockito.when(phoneNumberService.isValid(phoneNumber))
                .thenReturn(true);
        assertTrue(validator.isValid(phoneNumber, null));
        Mockito.verify(phoneNumberService, Mockito.times(1)).isValid(phoneNumber);
    }

    @Test
    void shouldBeInValid() {
        String phoneNumber = "123";
        Mockito.when(phoneNumberService.isValid(phoneNumber))
                .thenReturn(false);
        assertFalse(validator.isValid(phoneNumber, null));
        Mockito.verify(phoneNumberService, Mockito.times(1)).isValid(phoneNumber);
    }
}