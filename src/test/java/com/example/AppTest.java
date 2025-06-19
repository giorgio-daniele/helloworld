package com.example;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class AppTest 
{

    @Test
    public void shouldAnswerWithTrue()
    {
        assertTrue( true );
    }

    @Test
    void testRotateRight_basic() {
        int[] arr = {1, 2, 3, 4, 5};
        App.rotateRight(arr, 2);
        assertArrayEquals(new int[]{4, 5, 1, 2, 3}, arr);
    }

    @Test
    void testRotateRight_kZero() {
        int[] arr = {1, 2, 3};
        App.rotateRight(arr, 0);
        assertArrayEquals(new int[]{1, 2, 3}, arr);
    }

    @Test
    void testRotateRight_kEqualsLength() {
        int[] arr = {1, 2, 3};
        App.rotateRight(arr, 3);
        assertArrayEquals(new int[]{1, 2, 3}, arr);
    }

    @Test
    void testRotateRight_kGreaterThanLength() {
        int[] arr = {1, 2, 3};
        App.rotateRight(arr, 5);
        assertArrayEquals(new int[]{2, 3, 1}, arr);
    }

    @Test
    void testRotateRight_emptyArray() {
        int[] arr = {};
        App.rotateRight(arr, 3);
        assertArrayEquals(new int[]{}, arr);
    }

}
