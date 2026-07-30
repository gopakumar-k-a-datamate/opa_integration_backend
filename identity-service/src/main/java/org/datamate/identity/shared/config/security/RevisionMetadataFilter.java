package org.datamate.identity.shared.config.security;

import com.datamate.bedrock.framework.common.auditing.envers.vo.RevisionMetadataHolder;
import com.datamate.bedrock.framework.common.auditing.service.MetadataProvider;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RevisionMetadataFilter implements Filter {

    private final MetadataProvider metadataProvider;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            RevisionMetadataHolder.set(metadataProvider);
            chain.doFilter(request, response);
        } finally {
            RevisionMetadataHolder.clear();
        }
    }
}
