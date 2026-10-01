package io.riverguard;

import io.riverguard.cli.RiverGuardCli;
import java.time.Clock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Profiles;

@SpringBootApplication
public class RiverGuardApplication {
    public static void main(String[] args) {
        var context = SpringApplication.run(RiverGuardApplication.class, args);
        if (context.getEnvironment().acceptsProfiles(Profiles.of("local-cli"))) {
            int result = context.getBean(RiverGuardCli.class).run(args);
            SpringApplication.exit(context, () -> result);
            System.exit(result);
        }
    }

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
