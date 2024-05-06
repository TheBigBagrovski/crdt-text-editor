//package okp.nic.vectorclock;
//
//
//import lombok.Getter;
//
//import java.util.ArrayList;
//import java.util.List;
//
//@Getter
//public class VersionVector {
//    private final List<Version> versions = new ArrayList<>();
//    private final Version localVersion;
//
//    public VersionVector(String siteId) {
//        localVersion = new Version(siteId);
//        versions.add(localVersion);
//    }
//
//    public int incrementLocalVersion() {
//        return localVersion.incrementCounter();
//    }
//
//    public void update(Version incomingVersion) {
//        Version existingVersion = getVersionFromVector(incomingVersion);
//        if (existingVersion == null) {
//            Version newVersion = new Version(incomingVersion.getSiteId());
//            newVersion.update(incomingVersion);
//            versions.add(newVersion);
//        } else {
//            existingVersion.update(incomingVersion);
//        }
//    }
//
//    public boolean hasBeenApplied(Version incomingVersion) {
//        Version localIncomingVersion = getVersionFromVector(incomingVersion);
//        boolean isIncomingInVersionVector = (localIncomingVersion != null);
//        if (!isIncomingInVersionVector) {
//            return false;
//        }
//        boolean isIncomingLower = incomingVersion.getCounter() <= localIncomingVersion.getCounter();
//        boolean isInExceptions = localIncomingVersion.getExceptions().contains(incomingVersion.getCounter());
//        return isIncomingLower && !isInExceptions;
//    }
//
//    public Version getVersionFromVector(Version version) {
//        Version existingVersion = null;
//        for (Version value : versions) {
//            if (version.getSiteId().equals(value.getSiteId())) {
//                existingVersion = value;
//                break;
//            }
//        }
//        return existingVersion;
//    }
//
//}