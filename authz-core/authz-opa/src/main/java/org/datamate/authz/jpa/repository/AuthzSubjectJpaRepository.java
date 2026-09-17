package org.datamate.authz.jpa.repository;

import org.datamate.authz.jpa.entity.AuthzSubjectJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuthzSubjectJpaRepository extends JpaRepository<AuthzSubjectJpaEntity, Long> {

    Optional<AuthzSubjectJpaEntity> findBySubjectTypeAndSubjectId(String subjectType, String subjectId);

    boolean existsBySubjectTypeAndSubjectIdAndDeletedAtIsNull(String subjectType, String subjectId);

    List<AuthzSubjectJpaEntity> findAllBySubjectTypeAndDeletedAtIsNull(String subjectType);
}
