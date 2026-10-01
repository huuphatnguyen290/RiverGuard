package io.riverguard.cli;

import java.util.Arrays;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("local-cli")
public class RiverGuardCli {
    public int run(String[] args) {
        var operations = Arrays.stream(args)
                .filter(arg -> !arg.startsWith("--spring.") && !arg.startsWith("--server."))
                .toList();
        if (operations.isEmpty() || operations.equals(java.util.List.of("--help"))) {
            System.out.println("RiverGuard local CLI: --help. Ingestion commands are added in Task 11.");
            return 0;
        }
        System.err.println("Unsupported RiverGuard operation. Use --help.");
        return 2;
    }
}
