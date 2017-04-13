package net.knasmueller.pathfinder.unit_tests;

import net.knasmueller.pathfinder.service.Communicator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
public class CommunicatorTests {
    Communicator communicator;
    @Before
    public void init() {
        communicator = new Communicator();
    }

    @Test
    public void test_twoSiblingsAdded_bothAvailable() {
        communicator.addSibling("127.0.0.1:1234");
        communicator.addSibling("127.0.0.1:1235");
        assert(communicator.getSiblings().contains("127.0.0.1:1234"));
        assert(communicator.getSiblings().contains("127.0.0.1:1235"));
    }

    @Test
    public void test_noSiblingIsAddedTwice() {
        assert(communicator.getSiblings().size() == 0);
        communicator.addSibling("127.0.0.1:1234");
        communicator.addSibling("127.0.0.1:1234");
        assert(communicator.getSiblings().size() == 1);
    }

    @Test
    public void test_removePreviouslyAddedSibling() {
        assert(communicator.getSiblings().size() == 0);
        communicator.addSibling("127.0.0.1:1234");
        assert(communicator.getSiblings().size() == 1);
        communicator.removeSibling("127.0.0.1:1234");
        assert(communicator.getSiblings().size() == 0);
    }

    @Test
    public void test_removeUnknownSibling_isIgnored() {
        assert(communicator.getSiblings().size() == 0);
        communicator.addSibling("127.0.0.1:1234");
        assert(communicator.getSiblings().size() == 1);
        communicator.removeSibling("127.0.0.1:1235");
        assert(communicator.getSiblings().size() == 1);
    }
}
