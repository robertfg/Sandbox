package bitsetDemo;

import java.util.BitSet;

public class Test {
	 
    private static final int NUM_BITS = 64;
    //every other bit set
    private static long setMask = 0xAAAAAAAAAAAAAAAAl;
 
    private static long nativeBitTest() {
        long bits = 0;
        int found = 0;
        bits |= setMask;
 
        long start = System.currentTimeMillis();
 
        for (int bit = 0; bit < NUM_BITS; ++bit) {
            if (((bits >>> bit) & 0x1) == 0) {
                ++found;
            }
        }
 
        return System.currentTimeMillis() - start;
    }
 
    /**
     * @return
     */
    private static long bitsetBitTest() {
        BitSet bits = new BitSet(NUM_BITS);
        for (int bit = 1; bit < NUM_BITS; bit += 2) {
            bits.set(bit);
        }
 
        long start = System.currentTimeMillis();
 
        int found = 0;
        for (int i = bits.nextClearBit(0); i < NUM_BITS; i = bits
                .nextClearBit(i + 1)) {
            ++found;
        }
         return System.currentTimeMillis() - start;
    }
 
    public static void main(String[] args) {
        double totalNative, totalBitset = totalNative = 0;
 
        for (double i = 0; i < 10000; ++i) {
            totalNative += nativeBitTest();
            totalBitset += bitsetBitTest();
        }
 
        System.out.println("Native: " + totalNative / 10000.0 + " BitSet: "
                + totalBitset / 10000.0);
    }
}