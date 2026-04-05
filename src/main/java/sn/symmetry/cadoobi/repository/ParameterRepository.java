package sn.symmetry.cadoobi.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.symmetry.cadoobi.domain.entity.Parameter;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ParameterRepository extends JpaRepository<Parameter, UUID> {

    Optional<Parameter> findByKey(String key);

    boolean existsByKey(String key);

    List<Parameter> findByCategory(String category);

    Page<Parameter> findByCategory(String category, Pageable pageable);

    List<Parameter> findByIsActiveTrue();

    List<Parameter> findByCategoryAndIsActiveTrue(String category);
}
