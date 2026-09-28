package com.datamate.bedrock.framework.storage.adapter.libreOffice;

import org.jodconverter.core.DocumentConverter;
import org.jodconverter.remote.RemoteConverter;
import org.jodconverter.remote.office.RemoteOfficeManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JodConverterConfig {

    @Value("${jodconverter.remote.url:http://localhost:8082}")
    private String remoteUrl;

    @Value("${jodconverter.remote.socket-timeout:60000}")
    private long socketTimeout;

    @Value("${jodconverter.remote.connect-timeout:5000}")
    private long connectTimeout;

    @Value("${jodconverter.remote.pool-size:5}")
    private int poolSize;

    @Bean(initMethod = "start", destroyMethod = "stop")
    public RemoteOfficeManager officeManager() {
        return RemoteOfficeManager.builder()
                .urlConnection(remoteUrl)
                .connectTimeout(connectTimeout)
                .socketTimeout(socketTimeout)
                .poolSize(poolSize)
                .build();
    }

    @Bean
    public DocumentConverter jodConverter(RemoteOfficeManager officeManager) {
        return RemoteConverter.builder()
                .officeManager(officeManager)
                .build();
    }
}
