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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(Parameterized.class)
public class TestPuzzle {

    private final JsonFactory jsonFactory = new JsonFactory();
    private final String name;

    @Parameterized.Parameters
    public static Collection<Object[]> getParams() {
        return Arrays.asList(new Object[][] {
            { "cd_buys.json" },
            { "present_time.json" },
            { "light_work.json" },
            { "zebra.json" },
        });
    }

    public TestPuzzle(String name) {
        this.name = name;
    }

    @Test
    public void test() {
        try {
            final Puzzle solved = new Puzzle(jsonFactory.createJsonParser(Puzzle.class.getResourceAsStream(name)));
            final Puzzle test = new Puzzle(jsonFactory.createJsonParser(Puzzle.class.getResourceAsStream(name)));

            test.solve();
            //puzzle.write(jsonFactory.createJsonGenerator(System.out));

            assertEquals("Incorrect solution", solved.getSolutionPairs(), test.getSolutionPairs());
        } catch (IOException e) {
            fail("Failed to parse puzzle");
        } catch (ContradictionException e) {
            fail("Unexpected contradiction");
        } catch (TimeoutException e) {
            fail("Unexpected timeout");
        }
    }

}
