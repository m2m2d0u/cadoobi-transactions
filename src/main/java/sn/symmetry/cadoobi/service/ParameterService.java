package sn.symmetry.cadoobi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.symmetry.cadoobi.domain.entity.Parameter;
import sn.symmetry.cadoobi.dto.CreateParameterRequest;
import sn.symmetry.cadoobi.dto.ParameterResponse;
import sn.symmetry.cadoobi.dto.UpdateParameterRequest;
import sn.symmetry.cadoobi.exception.BusinessException;
import sn.symmetry.cadoobi.exception.DuplicateResourceException;
import sn.symmetry.cadoobi.exception.ResourceNotFoundException;
import sn.symmetry.cadoobi.repository.ParameterRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParameterService {

    private final ParameterRepository parameterRepository;

    @Transactional(readOnly = true)
    public List<ParameterResponse> getAllParameters() {
        return parameterRepository.findAll().stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public Page<ParameterResponse> getAllParameters(Pageable pageable) {
        return parameterRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<ParameterResponse> getParametersByCategory(String category) {
        return parameterRepository.findByCategory(category).stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public ParameterResponse getParameterById(UUID id) {
        return toResponse(findById(id));
    }

    @Transactional(readOnly = true)
    public ParameterResponse getParameterByKey(String key) {
        Parameter parameter = parameterRepository.findByKey(key)
            .orElseThrow(() -> new ResourceNotFoundException("Parameter not found with key: " + key));
        return toResponse(parameter);
    }

    @Transactional
    public ParameterResponse createParameter(CreateParameterRequest request) {
        if (parameterRepository.existsByKey(request.getKey())) {
            throw new DuplicateResourceException("Parameter with key '" + request.getKey() + "' already exists");
        }

        Parameter parameter = Parameter.builder()
            .key(request.getKey())
            .value(request.getValue())
            .category(request.getCategory())
            .description(request.getDescription())
            .isActive(request.getIsActive() != null ? request.getIsActive() : true)
            .isSystem(request.getIsSystem() != null ? request.getIsSystem() : false)
            .build();

        parameter = parameterRepository.save(parameter);
        log.info("Created parameter: key={}, category={}", parameter.getKey(), parameter.getCategory());
        return toResponse(parameter);
    }

    @Transactional
    public ParameterResponse updateParameter(UUID id, UpdateParameterRequest request) {
        Parameter parameter = findById(id);

        parameter.setValue(request.getValue());
        if (request.getCategory() != null) {
            parameter.setCategory(request.getCategory());
        }
        if (request.getDescription() != null) {
            parameter.setDescription(request.getDescription());
        }
        if (request.getIsActive() != null) {
            parameter.setIsActive(request.getIsActive());
        }

        parameter = parameterRepository.save(parameter);
        log.info("Updated parameter: id={}, key={}", id, parameter.getKey());
        return toResponse(parameter);
    }

    @Transactional
    public void deleteParameter(UUID id) {
        Parameter parameter = findById(id);

        if (parameter.getIsSystem()) {
            throw new BusinessException("Cannot delete system parameter: " + parameter.getKey());
        }

        parameterRepository.delete(parameter);
        log.info("Deleted parameter: id={}, key={}", id, parameter.getKey());
    }

    /**
     * Get parameter value by key, returns null if not found
     */
    public String getValueByKey(String key) {
        return parameterRepository.findByKey(key)
            .map(Parameter::getValue)
            .orElse(null);
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    private Parameter findById(UUID id) {
        return parameterRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Parameter not found with id: " + id));
    }

    private ParameterResponse toResponse(Parameter parameter) {
        return ParameterResponse.builder()
            .id(parameter.getId())
            .key(parameter.getKey())
            .value(parameter.getValue())
            .category(parameter.getCategory())
            .description(parameter.getDescription())
            .isActive(parameter.getIsActive())
            .isSystem(parameter.getIsSystem())
            .createdAt(parameter.getCreatedAt())
            .updatedAt(parameter.getUpdatedAt())
            .build();
    }
}
