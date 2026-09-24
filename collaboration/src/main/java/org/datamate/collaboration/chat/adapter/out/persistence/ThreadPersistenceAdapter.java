package org.datamate.collaboration.chat.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import org.datamate.collaboration.chat.application.port.out.ThreadRepositoryPort;
import org.datamate.collaboration.chat.domain.model.Thread;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Outgoing Adapter. Implements the Port using JPA technology.
 */
@Component
@RequiredArgsConstructor
public class ThreadPersistenceAdapter implements ThreadRepositoryPort {

    private final ThreadJpaRepository repository;

    @Override
    public Thread save(Thread thread) {
        ThreadJpaEntity entity = new ThreadJpaEntity();
        entity.setId(thread.getId());
        repository.save(entity);
        return thread;
    }

    @Override
    public Optional<Thread> findById(UUID id) {
        return repository.findById(id)
                .map(entity -> new Thread(entity.getId()));
    }
}
