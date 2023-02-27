package com.otpless;

import org.junit.Test;

import static org.junit.Assert.*;

import com.otpless.utils.Utility;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class UtilityUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void test_isValidString() {
        String name = null;
        assert !Utility.isValid(name);
        assert !Utility.isValid(null, "");
        assert !Utility.isValid("", "");
        assert !Utility.isValid("Otpless", "");
        assert Utility.isValid("otpless", "authlink");
    }
}