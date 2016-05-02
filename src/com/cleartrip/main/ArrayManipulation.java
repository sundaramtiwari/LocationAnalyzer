package com.cleartrip.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ArrayManipulation {

    public static void main(String[] args) throws IOException {

        //File file = new File("/home/sundaramtiwari/Documents/CodeEval/ArrayManipulation.txt");
        //BufferedReader buffer = new BufferedReader(new FileReader(file));
        BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));
        String line = buffer.readLine();
        int numOfTestCases = Integer.parseInt(line);
        List<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i < numOfTestCases; i++) {
            String[] inputArr = buffer.readLine().split(" ");

            if (inputArr[0].equals("i")) {
                int size = list.size();
                int value = Integer.parseInt(inputArr[1]);
                try {
                    if (!list.isEmpty() && list.get(size) != null)
                        list.remove(size-1);
                } catch (Exception e) {
                }
                list.add(value);
            } else if (inputArr[0].equals("u")) {
                // u p v : set the element at position p to v.
                int index = Integer.parseInt(inputArr[1]) - 1;
                int value = Integer.parseInt(inputArr[2]);
                try {
                    list.remove(index);
                } catch (Exception e) {
                }
                ;
                list.add(index, value);
            } else if (inputArr[0].equals("d")) {
                // d p : delete the element at position p.
                int index = Integer.parseInt(inputArr[1]) - 1;
                try {
                    list.remove(index);
                } catch (Exception e) {
                }
                ;
            } else if (inputArr[0].equals("q")) {
                // q l r : minimum of a particular range in the array.
                int fromIndex = Integer.parseInt(inputArr[1]) - 1;
                int toIndex = Integer.parseInt(inputArr[2]);
                try {
                    if (list.size() < toIndex) {
                        toIndex = list.size();
                    }
                    List<Integer> subList = list.subList(fromIndex, toIndex);
                    Collections.sort(subList);
                    System.out.println(subList.get(0));
                } catch (Exception e) {
                }
                ;
            }
        }
    }

}
