package org.datamate.authz.starter.enforcement;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.datamate.authz.annotation.ProtectedResource;
import org.datamate.authz.enforcement.PolicyEnforcer;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ProtectedResourceAspect {

    private final PolicyEnforcer policyEnforcer;

    public ProtectedResourceAspect(PolicyEnforcer policyEnforcer) {
        this.policyEnforcer = policyEnforcer;
    }

    @Before("@annotation(protectedResource)")
    public void enforcePolicy(ProtectedResource protectedResource) {
        policyEnforcer.enforce(protectedResource.value());
    }
}
