package test;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Created by open on 06/04/2017.
 *
 */
public class Lambdas {
    public static void main(String[] args) {
        r();
    }

    static void r(){
        Runnable r = () -> System.out.println("hello world");
        r.run();

        new Thread(
                () -> System.out.println("hello world")
        ).start();

        Consumer<Integer> c = (Integer  x) -> { System.out.println(x);};

        BiConsumer<Integer, String> b = (Integer x, String y) -> System.out.println(x + " : " + y);

        Predicate<String> p = Objects::isNull;

        Callable<String> c1 = () -> "done";

    }
}
