package lamda.custom;

import java.util.function.BiFunction;

public class TriFunctionTest {

    public static void main(String[] args) {
        BiFunction<Integer, Long, String> bi = (x,y) -> ""+x+","+y;
        TriFunction<Boolean, Integer, Long, String> tri = (x,y,z) -> ""+x+","+y+","+z;


        System.out.println(bi.apply(1, 2L));
        System.out.println(tri.apply(false, 1, 2L));
    }
  }