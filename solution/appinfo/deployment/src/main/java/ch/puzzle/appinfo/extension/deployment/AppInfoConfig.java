package ch.puzzle.appinfo.extension.deployment;

import ch.puzzle.quarkus.training.extension.appinfo.AppInfoNames;
import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(name = AppInfoNames.EXTENSION_NAME, phase = ConfigPhase.BUILD_TIME)
public class AppInfoConfig {

    /**
     * Simple build information string
     */
    @ConfigItem
    String builtFor;

    /**
     * Include feature in build
     */
    @ConfigItem(defaultValue = "true")
    boolean recordBuildTime;

    /**
     * Always include this. By default this will only be included in dev and test.
     * Setting this to true will also include this in Prod
     */
    @ConfigItem(defaultValue = "false")
    boolean alwaysInclude;

    /**
     * Specify basePath for extension endpoint
     */
    @ConfigItem(defaultValue = AppInfoNames.EXTENSION_NAME)
    String basePath;
}
