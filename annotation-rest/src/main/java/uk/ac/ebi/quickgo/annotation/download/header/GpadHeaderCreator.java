package uk.ac.ebi.quickgo.annotation.download.header;

/**
 * @author Tony Wardell
 * Date: 22/05/2017
 * Time: 15:33
 * Created with IntelliJ IDEA.
 */
public class GpadHeaderCreator extends GTypeHeaderCreator {

    public static final String VERSION = "gpa-version: 1.1";

    public GpadHeaderCreator(Ontology ontology) {
        super(ontology);
    }

    @Override String version() {
        return VERSION;
    }
}
