package byteBuffer;

import java.nio.ByteBuffer;

public class SimpleExample
{
    public static void main(String[] argv)
    throws Exception
    {
        byte[] data = new byte[16];
        ByteBuffer buf = ByteBuffer.wrap(data);

        buf.putShort(0, (short)0x1234);
        buf.putInt(2, 0x12345678);
        buf.putLong(8, 0x1122334455667788L);

        for (int ii = 0 ; ii < data.length ; ii++)
            System.out.println(String.format("index %2d = %02x", ii, data[ii]));

        // demonstrates what happens if you don't keep track of your
        // offsets -- will retrieve the 2 bytes from the "short" value,
        // and the first two bytes of the "int" value
        System.out.println(String.format(
                "retrieving value from wrong index = %04x",
                buf.getInt(0)));
    }
}