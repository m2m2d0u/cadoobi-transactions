package sn.symmetry.cadoobi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.symmetry.cadoobi.domain.entity.Operator;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OperatorRepository extends JpaRepository<Operator, UUID> {

    Optional<Operator> findByCode(String code);

    List<Operator> findByIsActiveTrue();

    List<Operator> findByCountryAndIsActiveTrue(String country);

    boolean existsByCode(String code);
}
