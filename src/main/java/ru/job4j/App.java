package ru.job4j;

import java.util.ArrayList;
import java.util.List;

public class App {
    public static void main(String[] args) {
        List<Integer> list = List.of(1,2,3,4,5);
        System.out.println(list.subList(0, 2));
    }
}
