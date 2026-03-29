package org.tanchee.inngest;

public final class Version {
    private static final String VERSION = Version.class.getPackage().getImplementationVersion();
    private static final String VERSION_NOT_FOUND = "version-not-found";

    private Version() {}

    public static String getVersion() {
        return VERSION != null ? VERSION : VERSION_NOT_FOUND;
    }
}
