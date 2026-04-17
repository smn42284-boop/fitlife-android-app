package com.example.fitlife_sumyatnoe.models;

import org.junit.Test;
import static org.junit.Assert.*;

public class BMITest {

    @Test
    public void testBMICalculation_NormalWeight_ReturnsCorrectBMI() {
        float heightCm = 170;
        float weightKg = 70;
        float heightM = heightCm / 100;
        float bmi = weightKg / (heightM * heightM);
        assertEquals(24.22, bmi, 0.1);
    }

    @Test
    public void testBMICalculation_Underweight_ReturnsCorrectCategory() {
        float bmi = 17.5f;
        String category;
        if (bmi < 18.5) category = "Underweight";
        else if (bmi < 25) category = "Normal";
        else if (bmi < 30) category = "Overweight";
        else category = "Obese";
        assertEquals("Underweight", category);
    }

    @Test
    public void testBMICalculation_NormalWeight_ReturnsCorrectCategory() {
        float bmi = 22.5f;
        String category;
        if (bmi < 18.5) category = "Underweight";
        else if (bmi < 25) category = "Normal";
        else if (bmi < 30) category = "Overweight";
        else category = "Obese";
        assertEquals("Normal", category);
    }
}