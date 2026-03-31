package sn.symmetry.cadoobi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.symmetry.cadoobi.domain.entity.DefaultMerchantFee;

import java.util.List;
import java.util.UUID;

@Repository
public interface DefaultMerchantFeeRepository extends JpaRepository<DefaultMerchantFee, UUID> {

    List<DefaultMerchantFee> findByIsActiveTrue();
}
