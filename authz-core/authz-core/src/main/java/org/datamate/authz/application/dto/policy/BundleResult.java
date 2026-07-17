package org.datamate.authz.application.dto.policy;

public record BundleResult(
        byte[] data,
        String etag,
        boolean notModified,
        boolean isEmpty
) {
    public static BundleResult empty() {
        return new BundleResult(null, null, false, true);
    }

    public static BundleResult notModified(String etag) {
        return new BundleResult(null, etag, true, false);
    }

    public static BundleResult success(byte[] data, String etag) {
        return new BundleResult(data, etag, false, false);
    }
}
