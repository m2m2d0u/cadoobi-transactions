package sn.symmetry.cadoobi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.symmetry.cadoobi.domain.entity.SystemAccount;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SystemAccountRepository extends JpaRepository<SystemAccount, UUID> {

    Optional<SystemAccount> findByCurrency(String currency);
}
