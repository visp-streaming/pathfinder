package net.knasmueller.pathfinder.unit_tests;

import net.knasmueller.pathfinder.service.PathFinderCommunicator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
public class PathFinderCommunicatorTests {
    PathFinderCommunicator pathFinderCommunicator;
    @Before
    public void init() {
        pathFinderCommunicator = new PathFinderCommunicator();
    }

    @Test
    public void test_twoSiblingsAdded_bothAvailable() {
        pathFinderCommunicator.addSibling("127.0.0.1:1234");
        pathFinderCommunicator.addSibling("127.0.0.1:1235");
        assert(pathFinderCommunicator.getSiblings().contains("127.0.0.1:1234"));
        assert(pathFinderCommunicator.getSiblings().contains("127.0.0.1:1235"));
    }

    @Test
    public void test_noSiblingIsAddedTwice() {
        assert(pathFinderCommunicator.getSiblings().size() == 0);
        pathFinderCommunicator.addSibling("127.0.0.1:1234");
        pathFinderCommunicator.addSibling("127.0.0.1:1234");
        assert(pathFinderCommunicator.getSiblings().size() == 1);
    }

    @Test
    public void test_removePreviouslyAddedSibling() {
        assert(pathFinderCommunicator.getSiblings().size() == 0);
        pathFinderCommunicator.addSibling("127.0.0.1:1234");
        assert(pathFinderCommunicator.getSiblings().size() == 1);
        pathFinderCommunicator.removeSibling("127.0.0.1:1234");
        assert(pathFinderCommunicator.getSiblings().size() == 0);
    }

    @Test
    public void test_removeUnknownSibling_isIgnored() {
        assert(pathFinderCommunicator.getSiblings().size() == 0);
        pathFinderCommunicator.addSibling("127.0.0.1:1234");
        assert(pathFinderCommunicator.getSiblings().size() == 1);
        pathFinderCommunicator.removeSibling("127.0.0.1:1235");
        assert(pathFinderCommunicator.getSiblings().size() == 1);
    }
}
