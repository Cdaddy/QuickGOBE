package uk.ac.ebi.quickgo.ff.reader;

public interface RowReader {
    boolean read(String[] data) throws Exception;

    void open() throws Exception;

    void close() throws Exception;

    String[] getColumns() throws Exception;


    public interface ProgressMonitor {
        double getFraction();
    }
    public interface MonitorRowReader extends ProgressMonitor,RowReader {}

}
