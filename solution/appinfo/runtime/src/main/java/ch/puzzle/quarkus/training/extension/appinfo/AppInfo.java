package ch.puzzle.quarkus.training.extension.appinfo;

public class AppInfo {

    private String buildTime;
    private String builtFor;

    private String runBy;

    private String createTime;
    private String startupTime;
    private String currentTime;

    private String applicationName;
    private String applicationVersion;

    private String propertiesString;

    public String getBuildTime() {
        return buildTime;
    }

    public void setBuildTime(String buildTime) {
        this.buildTime = buildTime;
    }

    public String getBuiltFor() {
        return builtFor;
    }

    public void setBuiltFor(String builtFor) {
        this.builtFor = builtFor;
    }

    public String getRunBy() {
        return runBy;
    }

    public void setRunBy(String runBy) {
        this.runBy = runBy;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getStartupTime() {
        return startupTime;
    }

    public void setStartupTime(String startupTime) {
        this.startupTime = startupTime;
    }

    public String getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(String currentTime) {
        this.currentTime = currentTime;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getApplicationVersion() {
        return applicationVersion;
    }

    public void setApplicationVersion(String applicationVersion) {
        this.applicationVersion = applicationVersion;
    }

    public String getPropertiesString() {
        return propertiesString;
    }

    public void setPropertiesString(String propertiesString) {
        this.propertiesString = propertiesString;
    }

    String asHumanReadableString() {
        String format = "%-15s %s%n";

        return "AppInfo\n" +
                String.format(format, "buildTime", buildTime) +
                String.format(format, "builtFor", builtFor) +

                String.format(format, "runBy", runBy) +
                String.format(format, "createTime", createTime) +
                String.format(format, "startupTime", startupTime) +

                String.format(format, "name", applicationName) +
                String.format(format, "version", applicationVersion) +

                String.format(format, "currentTime", currentTime) +

                "\n\nProperties\n" +
                propertiesString;
    }
}
