package org.datamate.collaboration.chat.application.usecase;

import lombok.RequiredArgsConstructor;
import org.datamate.collaboration.chat.application.port.in.CreateThreadUseCase;
import org.datamate.collaboration.chat.application.port.out.ThreadRepositoryPort;
import org.datamate.collaboration.chat.domain.model.Thread;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Application Service. Orchestrates the Domain objects and calls Outgoing Ports.
 */
@Service
@RequiredArgsConstructor
public class CreateThreadService implements CreateThreadUseCase {

    private final ThreadRepositoryPort repositoryPort;

    @Override
    @Transactional
    public Thread create(UUID id) {
        return repositoryPort.findById(id)
                .orElseGet(() -> repositoryPort.save(new Thread(id)));
    }
}
