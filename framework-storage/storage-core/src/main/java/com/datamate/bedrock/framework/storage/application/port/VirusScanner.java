package com.datamate.bedrock.framework.storage.application.port;

import com.datamate.bedrock.framework.storage.application.dto.ScanResult;
import java.io.InputStream;
public interface VirusScanner {
    ScanResult scan(InputStream inputStream);
}
