package ca.digitalcave.logic.domain;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.TimeoutException;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(Parameterized.class)
public class TestPuzzle {

    private final JsonFactory jsonFactory = new JsonFactory();
    private final String name;

    @Parameterized.Parameters
    public static Collection<Object[]> getParams() {
        return Arrays.asList(new Object[][] {
            { "test1.json" },
            { "test2.json" },
            { "test3.json" },
            { "small.json" },
            //{ "zepra.json" },
        });
    }

    public TestPuzzle(String name) {
        this.name = name;
    }

    @Test
    public void test() {
        try {
            final JsonParser p = jsonFactory.createJsonParser(Puzzle.class.getResourceAsStream(name));
            final Puzzle puzzle = new Puzzle(p);

            puzzle.solve();
            //puzzle.write(jsonFactory.createJsonGenerator(System.out));
            assertTrue("No items in solution", puzzle.getSolution().size() > 0);
        } catch (IOException e) {
            fail("Failed to parse puzzle");
        } catch (ContradictionException e) {
            fail("Unexpected contradiction");
        } catch (TimeoutException e) {
            fail("Unexpected timeout");
        }
    }
}
