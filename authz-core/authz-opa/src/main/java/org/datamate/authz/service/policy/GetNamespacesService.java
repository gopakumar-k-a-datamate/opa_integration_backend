package org.datamate.authz.service.policy;

import lombok.RequiredArgsConstructor;
import org.datamate.authz.service.policy.GetNamespacesService;
import org.datamate.authz.application.port.out.ResourceRepositoryPort;
import org.datamate.authz.model.policy.entity.Resource;
import org.springframework.stereotype.Service;
import org.datamate.authz.application.port.in.GetNamespacesUseCase;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class GetNamespacesService implements GetNamespacesUseCase {

    private final ResourceRepositoryPort resourcePort;

    
    public List<String> getNamespaces() {
        return resourcePort.findAllActive()
                .stream()
                .map(Resource::getNamespace)
                .distinct()
                .sorted()
                .toList();
    }
}
