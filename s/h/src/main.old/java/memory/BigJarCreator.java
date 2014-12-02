package memory;
/*
I came across a more subtle kind of resource leak recently. We open resources 
via class loader's getResourceAsStream and it happened that the input stream handles were not closed.

Uhm, you might say, what an idiot.

Well, what makes this interesting is: this way,
you can leak heap memory of the underlying process, rather than from JVM's heap.

All you need is a jar file with a file inside which will be referenced from Java code. 
The bigger the jar file, the quicker memory gets allocated.

You can easily create such a jar with the following class:
*/
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class BigJarCreator {
    public static void main(String[] args) throws IOException {
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(new File("big.jar")));
        zos.putNextEntry(new ZipEntry("resource.txt"));
        zos.write("not too much in here".getBytes());
        zos.closeEntry();
        zos.putNextEntry(new ZipEntry("largeFile.out"));
        for (int i=0 ; i<10000000 ; i++) {
            zos.write((int) (Math.round(Math.random()*100)+20));
        }
        zos.closeEntry();
        zos.close();
    }
}