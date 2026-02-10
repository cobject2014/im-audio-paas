package com.imaudiopaas.tts.repository;

import com.imaudiopaas.tts.core.domain.ProviderType;
import com.imaudiopaas.tts.model.ProviderConfig;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProviderConfigRepository extends JpaRepository<ProviderConfig, UUID> {
    
    Optional<ProviderConfig> findFirstByProviderTypeAndIsActiveTrue(ProviderType providerType);
    
    List<ProviderConfig> findByIsActiveTrue();
    
    boolean existsByName(String name);
}
