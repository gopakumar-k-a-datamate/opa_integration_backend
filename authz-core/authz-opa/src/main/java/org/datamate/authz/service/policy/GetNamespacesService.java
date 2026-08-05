package org.datamate.authz.service.policy;

import lombok.RequiredArgsConstructor;
import org.datamate.authz.service.policy.GetNamespacesService;
import org.datamate.authz.api.policy.ResourceRepository;
import org.datamate.authz.model.policy.entity.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class GetNamespacesService {

    private final ResourceRepository resourcePort;

    
    public List<String> getNamespaces() {
        return resourcePort.findAllActive()
                .stream()
                .map(Resource::getNamespace)
                .distinct()
                .sorted()
                .toList();
    }
}
