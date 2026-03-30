package sn.symmetry.cadoobi.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.symmetry.cadoobi.domain.entity.Merchant;
import sn.symmetry.cadoobi.domain.enums.MerchantStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MerchantRepository extends JpaRepository<Merchant, UUID> {

    Optional<Merchant> findByCode(String code);

    List<Merchant> findByStatus(MerchantStatus status);

    Page<Merchant> findByStatus(MerchantStatus status, Pageable pageable);

    List<Merchant> findByCountry(String country);

    boolean existsByCode(String code);

    boolean existsByEmail(String email);

    List<Merchant> findByUserId(UUID userId);

    Page<Merchant> findByUserId(UUID userId, Pageable pageable);
}
