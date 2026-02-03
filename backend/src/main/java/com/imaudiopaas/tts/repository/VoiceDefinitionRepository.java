package com.imaudiopaas.tts.repository;

import com.imaudiopaas.tts.model.VoiceDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VoiceDefinitionRepository extends JpaRepository<VoiceDefinition, String> {
}
