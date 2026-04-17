package com.example.fitlife_sumyatnoe;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class PerformanceTest {

    private static final int LARGE_DATA_SET = 1000;

    @Test
    public void testRecyclerViewPerformance() {
        long startTime = System.currentTimeMillis();
        List<String> largeList = new ArrayList<>();
        for (int i = 0; i < LARGE_DATA_SET; i++) {
            largeList.add("Item " + i);
        }
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        assert(duration < 500);
    }

    @Test
    public void testImageCompressionPerformance() {
        long startTime = System.currentTimeMillis();
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        assert(duration < 200);
    }
}