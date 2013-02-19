package kiwi.core.services.prefix;

import org.junit.Before;

public class PrefixServiceCCTests extends PrefixServiceTests {

    @Before
    @Override
    public void setup() {
        prefixService = new PrefixServiceCC();
    }

}
