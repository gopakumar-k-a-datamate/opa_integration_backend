package org.datamate.authz.application.usecase.policy;

import lombok.RequiredArgsConstructor;
import org.datamate.authz.application.port.in.policy.GetNamespacesUseCase;
import org.datamate.authz.application.port.out.policy.ResourcePersistencePort;
import org.datamate.authz.domain.model.policy.entity.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class GetNamespacesService implements GetNamespacesUseCase {

    private final ResourcePersistencePort resourcePort;

    @Override
    public List<String> getNamespaces() {
        return resourcePort.findAllActive()
                .stream()
                .map(Resource::getNamespace)
                .distinct()
                .sorted()
                .toList();
    }
}
