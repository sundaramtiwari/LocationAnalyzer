package com.cleartrip.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class AnagramTest {

    private static Map<Character, Integer> primeMap = new HashMap<Character, Integer>(26);
    
    static {
        primeMap.put('a', 2);
        primeMap.put('b', 3);
        primeMap.put('c', 5);
        primeMap.put('d', 7);
        primeMap.put('e', 11);
        primeMap.put('f', 13);
        primeMap.put('g', 17);
        primeMap.put('h', 19);
        primeMap.put('i', 23);
        primeMap.put('j', 29);
        primeMap.put('k', 31);
        primeMap.put('l', 37);
        primeMap.put('m', 41);
        primeMap.put('n', 43);
        primeMap.put('o', 47);
        primeMap.put('p', 53);
        primeMap.put('q', 59);
        primeMap.put('r', 61);
        primeMap.put('s', 67);
        primeMap.put('t', 71);
        primeMap.put('u', 73);
        primeMap.put('v', 79);
        primeMap.put('w', 83);
        primeMap.put('x', 89);
        primeMap.put('y', 97);
        primeMap.put('z', 101);
        primeMap.put('0', 103);
        primeMap.put('1', 107);
        primeMap.put('2', 109);
        primeMap.put('3', 113);
        primeMap.put('4', 127);
        primeMap.put('5', 131);
        primeMap.put('6', 137);
        primeMap.put('7', 139);
        primeMap.put('8', 149);
        primeMap.put('9', 151);
    }

    @SuppressWarnings("resource")
    public static void main(String[] args) throws IOException {
        
        File file = new File("/home/sundaramtiwari/Documents/CodeEval/Anagrams.txt");
        BufferedReader buffer = new BufferedReader(new FileReader(file));
        //BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));
        String line = buffer.readLine();
        int numOfTestCases = Integer.parseInt(line);
        
        for (int i=0; i<numOfTestCases; i++) {
            String[] inputArr = buffer.readLine().split(" ");
            isAnagram(inputArr[0], inputArr[1]);
        }
    }

    private static void isAnagram(String str1, String str2) {
        boolean flag = getPrimeProduct(str1) == getPrimeProduct(str2) ? true : false;
        if (flag) {
            System.out.println("YES");
        } else {
            System.out.println("NO");
        }
    }

    private static long getPrimeProduct(String str1) {
        if (str1.length() == 0)
            return 0;
        long product = 1;
        char[] charArray = str1.toCharArray();
        
        for (int i = 0; i < charArray.length; i++) {
            product *= primeMap.get(charArray[i]);
        }
        return product;
    }

}
