package org.datamate.authz.enforcement;

import org.datamate.authz.annotation.ProtectedResource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProtectedResourceAspectTest {

    @Mock
    private PolicyEnforcer policyEnforcer;

    @InjectMocks
    private ProtectedResourceAspect aspect;

    @ProtectedResource("finance:invoice:read")
    static class DummyAnnotationHolder {
    }

    @Test
    void enforcePolicy_delegatesToEnforcer() {
        ProtectedResource annotation = DummyAnnotationHolder.class.getAnnotation(ProtectedResource.class);
        
        aspect.enforcePolicy(annotation);
        
        verify(policyEnforcer).enforce("finance:invoice:read");
    }
}
