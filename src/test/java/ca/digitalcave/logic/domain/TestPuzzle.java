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
                { "01_present_time.json" },
                { "02_housing_problem.json" },
                { "03_visitors.json" },
                { "04_box_of_dates.json" },
                { "07_cd_buys.json" },
                { "25_light_work.json" },
                { "zebra.json" },
        });
    }

    public TestPuzzle(String name) {
        this.name = name;
    }

    @Test
    public void test() {
        try {
            final Puzzle puzzle = new Puzzle(jsonFactory.createJsonParser(Puzzle.class.getResourceAsStream(name)));

            puzzle.solve();

            System.out.println(puzzle.getSolutionTuples());
            //puzzle.write(jsonFactory.createJsonGenerator(System.out));

            if (!puzzle.getExpectedPairs().isEmpty()) {
                assertEquals("Incorrect solution pairs", puzzle.getExpectedPairs(), puzzle.getSolutionPairs());
            }
            if (!puzzle.getExpectedTuples().isEmpty()) {
                assertEquals("Incorrect solution tuples", puzzle.getExpectedTuples(), puzzle.getSolutionTuples());
            }
        } catch (IOException e) {
            fail("Failed to parse puzzle");
        } catch (ContradictionException e) {
            fail("Unexpected contradiction");
        } catch (TimeoutException e) {
            fail("Unexpected timeout");
        }
    }

}
