package com.example;

public class App {
    public static void main(String[] args) {

        // Bug: possibile NullPointerException
        String name = null;
        System.out.println(name.toLowerCase());

        // Duplicazione: due stampe identiche
        System.out.println("Duplicated line");
        System.out.println("Duplicated line");
    }
}
