package org.datamate.authz.domain.service.policy;

import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPOutputStream;

/**
 * Domain service that packages Rego policy text into an OPA-compatible
 * {@code bundle.tar.gz} byte array using only the Java standard library.
 *
 * <p>This is a pure computation service (no I/O side-effects beyond in-memory bytes)
 * and therefore belongs in the domain layer.</p>
 *
 * <h3>POSIX ustar Tar Format</h3>
 * Each file entry is written as:
 * <ol>
 *   <li>512-byte header (name, permissions, size, mtime, checksum, type flag, magic)</li>
 *   <li>File data bytes</li>
 *   <li>Padding to the next 512-byte boundary</li>
 * </ol>
 * The archive ends with two consecutive 512-byte zero blocks.
 */
@Component
public class TarGzBundleBuilder {

    private static final int BLOCK = 512;
    private static final String REGO_ENTRY = "authz/policy.rego";

    /**
     * Packages the given Rego content into a {@code bundle.tar.gz}.
     *
     * @param regoContent full Rego policy text
     * @return binary {@code bundle.tar.gz} content
     */
    public byte[] build(String namespace, String regoContent) throws IOException {
        byte[] regoBytes = regoContent.getBytes(StandardCharsets.UTF_8);
        String manifestContent = "{\"roots\": [\"app/authz/" + namespace + "\"]}";
        byte[] manifestBytes = manifestContent.getBytes(StandardCharsets.UTF_8);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(baos)) {
            writeTarEntry(gzip, REGO_ENTRY, regoBytes);
            writeTarEntry(gzip, ".manifest", manifestBytes);
            gzip.write(new byte[BLOCK * 2]); // end-of-archive marker
        }
        return baos.toByteArray();
    }

    private void writeTarEntry(OutputStream out, String name, byte[] data) throws IOException {
        out.write(buildHeader(name, data.length));
        out.write(data);
        int pad = data.length % BLOCK;
        if (pad != 0) out.write(new byte[BLOCK - pad]);
    }

    /**
     * Builds a 512-byte POSIX ustar tar header.
     * Checksum is computed as the unsigned sum of all header bytes
     * (treating the checksum field itself as 8 spaces during calculation).
     */
    private byte[] buildHeader(String name, long size) {
        byte[] h = new byte[BLOCK];
        putAscii(h, 0,   name,         100);  // name
        putAscii(h, 100, "0000644\0",   8);    // mode (rw-r--r--)
        putAscii(h, 108, "0000000\0",   8);    // uid
        putAscii(h, 116, "0000000\0",   8);    // gid
        putAscii(h, 124, toOctal(size, 11), 12);  // file size
        putAscii(h, 136, toOctal(System.currentTimeMillis() / 1000L, 11), 12); // mtime
        for (int i = 148; i < 156; i++) h[i] = ' '; // checksum placeholder
        h[156] = '0'; // type: regular file
        putAscii(h, 257, "ustar ", 6); // magic
        putAscii(h, 263, "  ", 2);     // version

        // Compute and write checksum
        int checksum = 0;
        for (byte b : h) checksum += (b & 0xFF);
        putAscii(h, 148, String.format("%06o\0 ", checksum), 8);

        return h;
    }

    private void putAscii(byte[] buf, int offset, String value, int maxLen) {
        byte[] bytes = value.getBytes(StandardCharsets.US_ASCII);
        System.arraycopy(bytes, 0, buf, offset, Math.min(bytes.length, maxLen));
    }

    private String toOctal(long value, int digits) {
        return String.format("%" + digits + "s", Long.toOctalString(value)).replace(' ', '0');
    }
}

