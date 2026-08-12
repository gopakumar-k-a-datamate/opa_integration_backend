package org.datamate.authz.application.port.in;

import java.util.List;

public interface GetNamespacesUseCase {
    List<String> getNamespaces();
}
