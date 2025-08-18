package info.kgeorgiy.ja.morozov.arrayset;

import java.util.*;
import java.util.function.IntConsumer;

public class SimpleTests {

    private static void printSets(NavigableSet<?>... sets) {
        for (NavigableSet<?> set : sets) {
            if (set.isEmpty()) {
                System.out.print("[]");
            } else {
                for (Object o : set) {
                    System.out.print(o.toString() + " ");
                }
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        Comparator<Integer> comparator = Comparator.comparingInt(o -> o % 3);

        ArraySet<Integer> set = new ArraySet<>(
                List.of(3, 10, 290, 32, 1, 0, -5), comparator);

        ArraySet<String> set2 = new ArraySet<>();

        ArraySet<Boolean> set3 = new ArraySet<>(List.of(true, false, true));

        NavigableSet<Integer> subSet =
                set.subSet(-50, false, -45, true);

        //ArraySet<Integer> simpleSet = new ArraySet<>(null);

        Set<Integer> intSet = new TreeSet<>(List.of(1, 2, 3));

        Map<List<Integer>, List<Integer>> map = new TreeMap<>();
        //map.put(List.of(0), List.of(0));


        //intSet.add(239);

        //System.out.println(intSet.containsAll(simpleSet));

        //System.out.println(simpleSet.containsAll(intSet));

        //printSets(set, set2, set3, subSet);

        int[] array = {1, 2, 3, 4, 5, 6, 7, 8};
        Spliterator.OfInt spliterator = Arrays.spliterator(array);
        spliterator.tryAdvance((IntConsumer) System.out::println);
        spliterator.tryAdvance((IntConsumer) System.out::println);
        spliterator.tryAdvance((IntConsumer) System.out::println);
        spliterator.tryAdvance((IntConsumer) System.out::println);
        System.out.println("*");
        spliterator.trySplit();
        spliterator.forEachRemaining((IntConsumer) System.out::println);
    }
}
