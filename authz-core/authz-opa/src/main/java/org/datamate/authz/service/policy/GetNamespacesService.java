package org.datamate.authz.service.policy;
import org.datamate.authz.api.policy.ResourceRepository;
import org.datamate.authz.model.policy.entity.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
/* Todo- check exception management
separation of concern
logger if needed
necessity of transaction
 */
@Service
@Transactional(readOnly = true)
public class GetNamespacesService {

    public GetNamespacesService(ResourceRepository resourcePort) {
        this.resourcePort = resourcePort;
    }

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
