package com.datamate.bedrock.framework.storage.adapter.clamav;

import com.datamate.bedrock.framework.common.logging.annotation.EnableLogger;
import com.datamate.bedrock.framework.common.logging.service.Logger;
import com.datamate.bedrock.framework.storage.application.dto.ScanResult;
import com.datamate.bedrock.framework.storage.application.port.VirusScanner;
import com.datamate.bedrock.framework.storage.domain.exception.StorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Component
public class ClamAvVirusScanner implements VirusScanner {

    @EnableLogger
    private Logger log;

    private final String host;
    private final int port;
    private final int timeout;
    private final int connectionTimeout;

    private static final int BUFFER_SIZE = 8192;
    private static final int CHUNK_HEADER_SIZE = 4;

    public ClamAvVirusScanner(
            @Value("${clamav.host:localhost}") String host,
            @Value("${clamav.port:3310}") int port,
            @Value("${clamav.timeout:30000}") int timeout,
            @Value("${clamav.connection-timeout:5000}") int connectionTimeout) {
        this.host = host;
        this.port = port;
        this.timeout = timeout;
        this.connectionTimeout = connectionTimeout;
    }

    @Override
    public ScanResult scan(InputStream inputStream) {
        try {
            return performScan(inputStream);
        } catch (IOException ex) {
            if (log != null) {
                log.error("ClamAV communication error: {}", ex.getMessage());
            }
            throw new StorageException("Security scan service unavailable", ex);
        }
    }

    private ScanResult performScan(InputStream inputStream) throws IOException {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), connectionTimeout);
            socket.setSoTimeout(timeout);
            try (
                    OutputStream out = new BufferedOutputStream(socket.getOutputStream());
                    InputStream in = socket.getInputStream()
            ) {
                sendInstreamCommand(out);
                streamFile(inputStream, out);
                finishStream(out);
                String response = readResponse(in);
                if (log != null) {
                    log.debug("ClamAV raw response: {}", response);
                }
                return parseResponse(response);
            }
        }
    }

    private void sendInstreamCommand(OutputStream out) throws IOException {
        out.write("zINSTREAM\0".getBytes(StandardCharsets.US_ASCII));
        out.flush();
    }

    private void streamFile(InputStream inputStream, OutputStream out) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            byte[] sizeHeader = ByteBuffer.allocate(CHUNK_HEADER_SIZE)
                    .putInt(bytesRead)
                    .array();
            out.write(sizeHeader);
            out.write(buffer, 0, bytesRead);
        }
    }

    private void finishStream(OutputStream out) throws IOException {
        out.write(new byte[]{0, 0, 0, 0});
        out.flush();
    }

    private ScanResult parseResponse(String response) {
        if (response == null || response.isBlank()) {
            throw new StorageException("ClamAV returned an empty response");
        }
        if (response.contains("OK")) {
            return new ScanResult(true, null, LocalDateTime.now());
        }
        if (response.contains("FOUND")) {
            String virusName = response
                    .replace("stream:", "")
                    .replace("FOUND", "")
                    .trim();
            if (log != null) {
                log.warn("Malware detected: {}", virusName);
            }
            return new ScanResult(false, virusName, LocalDateTime.now());
        }
        throw new StorageException("Unexpected ClamAV response: " + response);
    }

    private String readResponse(InputStream in) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            result.write(buffer, 0, bytesRead);
            String partial = result.toString(StandardCharsets.US_ASCII);
            if (partial.contains("\n") || partial.contains("\0")) {
                break;
            }
        }
        return result.toString(StandardCharsets.US_ASCII).trim();
    }
}