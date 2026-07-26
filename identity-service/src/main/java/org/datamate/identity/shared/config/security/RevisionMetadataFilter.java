package org.datamate.identity.shared.config.security;

import com.datamate.bedrock.framework.common.auditing.envers.service.SecurityContexMetadataProvider;
import com.datamate.bedrock.framework.common.auditing.envers.vo.RevisionMetadataHolder;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RevisionMetadataFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            RevisionMetadataHolder.set(new SecurityContexMetadataProvider());
            chain.doFilter(request, response);
        } finally {
            RevisionMetadataHolder.clear();
        }
    }
}
