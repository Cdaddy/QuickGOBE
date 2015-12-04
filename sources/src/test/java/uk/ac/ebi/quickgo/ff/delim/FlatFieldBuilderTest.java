package uk.ac.ebi.quickgo.ff.delim;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static uk.ac.ebi.quickgo.ff.delim.FlatFieldBuilder.newFlatField;
import static uk.ac.ebi.quickgo.ff.delim.FlatFieldBuilder.parseFlatField;
import static uk.ac.ebi.quickgo.ff.delim.FlatFieldBuilder.parseFlatFieldToLevel;
import static uk.ac.ebi.quickgo.ff.delim.FlatFieldLeaf.newFlatFieldLeaf;

/**
 * Created 26/11/15
 * @author Edd
 */
public class FlatFieldBuilderTest {
    @Test
    public void createFlatFieldBuilder() {

        String flatField = newFlatField()
                .addField(newFlatFieldLeaf("1"))
                .addField(newFlatFieldLeaf("2"))
                .addField(
                        newFlatField()
                                .addField(newFlatFieldLeaf("a"))
                                .addField(newFlatFieldLeaf("b")))
                .addField(newFlatFieldLeaf("3")).buildString();
        assertThat(flatField, is(notNullValue()));
        assertThat(flatField.contains("1"), is(true));
        assertThat(flatField.contains("2"), is(true));
        assertThat(flatField.contains("a"), is(true));
        assertThat(flatField.contains("b"), is(true));
        assertThat(flatField.contains("3"), is(true));
        System.out.println(flatField);
    }

    @Test
    public void handleEmptyFieldsConsistently() {
        String origStr = newFlatField()
                .addField(newFlatFieldLeaf("1"))
                .addField(newFlatFieldLeaf(""))
                .addField(newFlatFieldLeaf("3")).buildString();
        System.out.println(origStr);

        FlatFieldBuilder flatFieldBuilderParsed = parseFlatField(origStr);
        String parsedStr = flatFieldBuilderParsed.buildString();
        System.out.println(parsedStr);

        assertThat(origStr, is(equalTo(parsedStr)));
    }

    @Test
    public void handleEmptyFieldsConsistentlyAtEnd() {
        FlatFieldBuilder origFlatFieldBuilder = newFlatField()
                .addField(newFlatFieldLeaf("XX:00001"))
                .addField(newFlatFieldLeaf("XX"))
                .addField(newFlatFieldLeaf("because it's also bad"))
                .addField(newFlatFieldLeaf())
                .addField(newFlatFieldLeaf("something else"))
                .addField(newFlatFieldLeaf());
        String origStr = origFlatFieldBuilder // no parameter means it's got no value
                .buildString();
        System.out.println(origStr);

        FlatFieldBuilder flatFieldBuilder = parseFlatField(origStr);
        assertThat(origFlatFieldBuilder.getFields().size(), is(equalTo(flatFieldBuilder.getFields().size())));
        assertThat(origFlatFieldBuilder.getFields().size(), is(6));
    }

    /** Check one can create a flat field object, write itself as a String A, then parse
     * this written value into a new flat field object, and write it again as String B. A and B
     * must be equal.
     */
    @Test
    public void parseFlatFieldBuilderString2NestingLevels() {
        FlatFieldBuilder flatFieldBuilderOrig = newFlatField()
                .addField(newFlatFieldLeaf("1"))
                .addField(newFlatFieldLeaf("2"))
                .addField(
                        newFlatField()
                                .addField(newFlatFieldLeaf("a"))
                                .addField(newFlatFieldLeaf("b")))
                .addField(newFlatFieldLeaf("3"));
        String origStr = flatFieldBuilderOrig.buildString(); // serialise

        FlatFieldBuilder flatFieldBuilderParsed = parseFlatField(origStr);
        String parsedStr = flatFieldBuilderParsed.buildString();

        assertThat(parsedStr, is(equalTo(parsedStr)));

        System.out.println(parsedStr);
    }

    @Test
    public void parseFlatFieldBuilderString3NestingLevels() {
        FlatFieldBuilder flatFieldBuilderOrig = newFlatField()
                .addField(newFlatFieldLeaf("level1:A"))
                .addField(newFlatFieldLeaf("level1:B"))
                .addField(
                        newFlatField()
                                .addField(newFlatFieldLeaf("level2:A"))
                                .addField(newFlatField()
                                        .addField(newFlatFieldLeaf("level3:A"))
                                        .addField(newFlatFieldLeaf("level3:B"))
                                        .addField(newFlatFieldLeaf("level3:C")))
                                .addField(newFlatFieldLeaf("level2:B")))
                .addField(newFlatFieldLeaf("level1:C"));
        String origStr = flatFieldBuilderOrig.buildString(); // serialise

        FlatFieldBuilder flatFieldBuilderParsed = parseFlatField(origStr);
        String parsedStr = flatFieldBuilderParsed.buildString();

        assertThat(parsedStr, is(equalTo(parsedStr)));

        System.out.println(parsedStr);
    }

    @Test
    public void parseFlatFieldBuilderString4NestingLevels() {
        FlatFieldBuilder flatFieldBuilderOrig = newFlatField()
                .addField(newFlatFieldLeaf("level1:A"))
                .addField(newFlatFieldLeaf("level1:B"))
                .addField(
                        newFlatField()
                                .addField(newFlatFieldLeaf("level2:A"))
                                .addField(newFlatField()
                                        .addField(newFlatFieldLeaf("level3:A"))
                                        .addField(newFlatFieldLeaf("level3:B"))
                                        .addField(newFlatFieldLeaf("level3:C")))
                                .addField(newFlatFieldLeaf("level2:B")))
                .addField(
                        newFlatField()
                                .addField(newFlatFieldLeaf("level2:C"))
                                .addField(newFlatField()
                                        .addField(newFlatFieldLeaf("level3:D"))
                                        .addField(newFlatField()
                                                .addField(newFlatFieldLeaf("level4:A"))
                                                .addField(newFlatFieldLeaf("level4:B")))
                                        .addField(newFlatFieldLeaf("level3:E")))
                                .addField(newFlatFieldLeaf("level2:C")))
                .addField(newFlatFieldLeaf("level1:C"));
        String origStr = flatFieldBuilderOrig.buildString(); // serialise

        FlatFieldBuilder flatFieldBuilderParsed = parseFlatField(origStr);
        String parsedStr = flatFieldBuilderParsed.buildString();

        assertThat(parsedStr, is(equalTo(parsedStr)));

        System.out.println(parsedStr);
    }

    @Test
    public void parseFirstLevelOfFlatString() {
        FlatFieldBuilder flatFieldBuilderOrig = newFlatField()
                .addField(newFlatFieldLeaf("level1:A"))
                .addField(newFlatFieldLeaf("level1:B"))
                .addField(
                        newFlatField()
                                .addField(newFlatFieldLeaf("level2:A"))
                                .addField(newFlatField()
                                        .addField(newFlatFieldLeaf("level3:A"))
                                        .addField(newFlatFieldLeaf("level3:B"))
                                        .addField(newFlatFieldLeaf("level3:C")))
                                .addField(newFlatFieldLeaf("level2:B")))
                .addField(
                        newFlatField()
                                .addField(newFlatFieldLeaf("level2:C"))
                                .addField(newFlatField()
                                        .addField(newFlatFieldLeaf("level3:D"))
                                        .addField(newFlatField()
                                                .addField(newFlatFieldLeaf("level4:A"))
                                                .addField(newFlatFieldLeaf("level4:B")))
                                        .addField(newFlatFieldLeaf("level3:E")))
                                .addField(newFlatFieldLeaf("level2:C")))
                .addField(newFlatFieldLeaf("level1:C"));
        String origStr = flatFieldBuilderOrig.buildString(); // serialise
        System.out.println(origStr);

        FlatFieldBuilder shallowVersion = parseFlatFieldToLevel(origStr, 0);

        String shallowVersionAsStr = shallowVersion.buildString();
        assertThat(origStr, is(equalTo(shallowVersionAsStr)));
    }

    @Test
    public void parseShallowFlatFieldAndCompareWithOriginal() {
        FlatFieldBuilder flatFieldBuilderOrig = newFlatField()
                .addField(newFlatFieldLeaf("level1:A"))
                .addField(newFlatFieldLeaf("level1:B"))
                .addField(
                        newFlatField()
                                .addField(newFlatFieldLeaf("level2:A"))
                                .addField(newFlatField()
                                        .addField(newFlatFieldLeaf("level3:A"))
                                        .addField(newFlatFieldLeaf("level3:B"))
                                        .addField(newFlatFieldLeaf("level3:C")))
                                .addField(newFlatFieldLeaf("level2:B")))
                .addField(
                        newFlatField()
                                .addField(newFlatFieldLeaf("level2:C"))
                                .addField(newFlatField()
                                        .addField(newFlatFieldLeaf("level3:D"))
                                        .addField(newFlatField()
                                                .addField(newFlatFieldLeaf("level4:A"))
                                                .addField(newFlatFieldLeaf("level4:B")))
                                        .addField(newFlatFieldLeaf("level3:E")))
                                .addField(newFlatFieldLeaf("level2:C")))
                .addField(newFlatFieldLeaf("level1:C"));
        String origStr = flatFieldBuilderOrig.buildString(); // serialise
        System.out.println(origStr);

        FlatFieldBuilder shallowVersion = parseFlatFieldToLevel(origStr, 0);

        String shallowVersionAsStr = shallowVersion.buildString();
        FlatFieldBuilder flatFieldBuilderFromShallow = parseFlatField(shallowVersionAsStr);

        assertThat(flatFieldBuilderOrig, is(equalTo(flatFieldBuilderFromShallow)));
    }
}