package io.riverguard.cli;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RiverGuardCliTest {
    @Test
    void helpSucceedsWithoutPretendingToIngestData() {
        assertEquals(0, new RiverGuardCli().run(new String[]{"--help"}));
    }

    @Test
    void unsupportedOperationReturnsFailure() {
        assertEquals(2, new RiverGuardCli().run(new String[]{"ingest-file", "--file=missing.json"}));
    }
}
