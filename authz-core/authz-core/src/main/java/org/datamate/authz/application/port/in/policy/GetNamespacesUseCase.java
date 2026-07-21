package org.datamate.authz.application.port.in.policy;

import java.util.List;

/**
 * Use case for retrieving available namespaces in the current module.
 */
public interface GetNamespacesUseCase {
    List<String> getNamespaces();
}
