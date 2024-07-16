package org.example;

import java.util.*;


public class Main {
    public static boolean frequencyCalculated = true;
    public static final Map<Integer, Integer> sizeToFreq = new HashMap<>();
    public static void main(String[] args) throws InterruptedException {
        final int NUM_ROUTES = 1000;
        final String LETTERS = "RLRFR";
        final int LENGTH = 100;

        List<Thread> threads = new ArrayList<>();
        Thread freqlogThread = new Thread(Main::logHighestFrequency);
        freqlogThread.start();
        for (int i = 0; i < NUM_ROUTES; i++) {
            Thread thread = new Thread(() -> generateRoute(LETTERS, LENGTH));
            threads.add(thread);
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
        freqlogThread.interrupt();

        //int highestFrequencyKey = Collections.max(sizeToFreq.entrySet(), Map.Entry.comparingByValue()).getKey();
        //System.out.printf("Самое частое количество повторений %d (встретилось %d раз)\n", highestFrequencyKey, sizeToFreq.get(highestFrequencyKey));
        System.out.print("Другие размеры:\n");
        for (Integer key : sizeToFreq.keySet().stream().sorted().toList()) {
            System.out.printf("- %d (%d раз)\n", key, sizeToFreq.get(key));
        }
    }

    public static void logHighestFrequency() {
        synchronized (sizeToFreq) {
            while (!Thread.interrupted()) {
                while (frequencyCalculated) {
                    try {
                        sizeToFreq.wait();
                    } catch (InterruptedException e) {
                        return;
                    }
                }
                int highestFrequencyKey = Collections.max(sizeToFreq.entrySet(), Map.Entry.comparingByValue()).getKey();
                System.out.printf("Самое частое количество повторений %d (встретилось %d раз)\n", highestFrequencyKey, sizeToFreq.get(highestFrequencyKey));
                frequencyCalculated = true;
                sizeToFreq.notifyAll();
            }
        }
    }

    public static String generateRoute(String letters, int length) {
        Random random = new Random();
        StringBuilder route = new StringBuilder();
        int countR = 0;

        for (int i = 0; i < length; i++) {
            char newLetter = letters.charAt(random.nextInt(letters.length()));
            if (newLetter == 'R') countR++;
            route.append(newLetter);
        }

        synchronized (sizeToFreq) {
            while (!frequencyCalculated) {
                try {
                    sizeToFreq.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            frequencyCalculated = false;
            //System.out.println(".");
            sizeToFreq.put(countR, sizeToFreq.getOrDefault(countR, 0) + 1);
            sizeToFreq.notifyAll();
        }

        return route.toString();
    }
}