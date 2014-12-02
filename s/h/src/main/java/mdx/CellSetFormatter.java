package mdx;

import org.olap4j.CellSet;
import java.io.PrintWriter;

public interface CellSetFormatter {
    /**
     * Formats a CellSet as text to a PrintWriter.
     *
     * @param cellSet Cell set
     * @param pw Print writer
     */
    void format(
        CellSet cellSet,
        PrintWriter pw);
}

// End CellSetFormatter.java
