package org.datamate.authz.jpa.repository;

import org.datamate.authz.jpa.entity.AuthzSubjectJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuthzSubjectJpaRepository extends JpaRepository<AuthzSubjectJpaEntity, Long> {

    Optional<AuthzSubjectJpaEntity> findBySubjectTypeAndSubjectId(String subjectType, String subjectId);

    boolean existsBySubjectTypeAndSubjectIdAndDeletedAtIsNull(String subjectType, String subjectId);

    List<AuthzSubjectJpaEntity> findAllBySubjectTypeAndDeletedAtIsNull(String subjectType);

    @Query("SELECT s FROM AuthzSubjectJpaEntity s " +
           "WHERE s.deletedAt IS NULL " +
           "AND (:subjectType IS NULL OR s.subjectType = CAST(:subjectType AS string)) " +
           "AND (:search IS NULL OR LOWER(s.subjectId) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
           "     OR LOWER(s.displayName) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
           "     OR LOWER(s.subjectName) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))")
    Page<AuthzSubjectJpaEntity> searchSubjects(
            @Param("subjectType") String subjectType,
            @Param("search") String search,
            Pageable pageable);
}
