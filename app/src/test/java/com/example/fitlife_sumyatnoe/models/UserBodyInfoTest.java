
package com.example.fitlife_sumyatnoe.models;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.example.fitlife_sumyatnoe.models.UserBodyInfo;

public class UserBodyInfoTest {

    private UserBodyInfo bodyInfo;

    @Before
    public void setUp() {
        bodyInfo = new UserBodyInfo();
        bodyInfo.setUserId("user123");
        bodyInfo.setHeightCm(175);
        bodyInfo.setWeightKg(70);
        bodyInfo.setGender("Male");
        bodyInfo.setBirthday(System.currentTimeMillis());
    }

    @Test
    public void testCalculateBMI() {
        float bmi = bodyInfo.getBmi();
        // BMI = weight(kg) / (height(m))^2 = 70 / (1.75^2) = 22.86
        assertEquals(22.86, bmi, 0.01);
    }

    @Test
    public void testBMICalculationWithZeroHeight() {
        bodyInfo.setHeightCm(0);
        float bmi = bodyInfo.getBmi();
        assertEquals(0, bmi, 0.01);
    }

    @Test
    public void testBMICalculationWithZeroWeight() {
        bodyInfo.setWeightKg(0);
        float bmi = bodyInfo.getBmi();
        assertEquals(0, bmi, 0.01);
    }

    @Test
    public void testGetBMICategoryUnderweight() {
        bodyInfo.setWeightKg(50);
        bodyInfo.setHeightCm(175);
        String status = bodyInfo.getBmiStatus();
        assertEquals("Underweight", status);
    }

    @Test
    public void testGetBMICategoryNormal() {
        bodyInfo.setWeightKg(70);
        bodyInfo.setHeightCm(175);
        String status = bodyInfo.getBmiStatus();
        assertEquals("Normal", status);
    }

    @Test
    public void testGetBMICategoryOverweight() {
        bodyInfo.setWeightKg(85);
        bodyInfo.setHeightCm(175);
        String status = bodyInfo.getBmiStatus();
        assertEquals("Overweight", status);
    }

    @Test
    public void testGetBMICategoryObese() {
        bodyInfo.setWeightKg(100);
        bodyInfo.setHeightCm(175);
        String status = bodyInfo.getBmiStatus();
        assertEquals("Obese", status);
    }

    @Test
    public void testCalculateAge() {
        // Set birthday to 25 years ago
        long twentyFiveYearsAgo = System.currentTimeMillis() - (25L * 365 * 24 * 60 * 60 * 1000);
        bodyInfo.setBirthday(twentyFiveYearsAgo);
        int age = bodyInfo.getAge();
        assertEquals(25, age);
    }
}