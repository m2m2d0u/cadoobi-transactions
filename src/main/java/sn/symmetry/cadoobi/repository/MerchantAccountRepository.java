package sn.symmetry.cadoobi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.symmetry.cadoobi.domain.entity.MerchantAccount;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MerchantAccountRepository extends JpaRepository<MerchantAccount, UUID> {

    Optional<MerchantAccount> findByMerchantIdAndCurrency(UUID merchantId, String currency);

    List<MerchantAccount> findByMerchantId(UUID merchantId);
}
