package com.logistic.ontologies.service.domain;

import java.util.List;
import java.util.UUID;

import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.logistic.ontologies.dto.domain.WeatherDTO;
import com.logistic.ontologies.repository.UserTaskRoleRepository;

@Service
public class WeatherService {
    @Autowired
    private UserTaskRoleRepository roleRepo;
    @Autowired
    private BaseDomainService helpers;

    @Transactional(readOnly = true)
    public WeatherDTO getWeather(UUID taskId, UUID userId, String weatherId) throws OWLOntologyCreationException {

        roleRepo.findByIdUserIdAndIdTaskId(userId, taskId)
            .orElseThrow(() -> new AccessDeniedException("No access"));

        OWLOntology task = helpers.loadTaskOntology(taskId);
        OWLNamedIndividual weather = helpers.requireIndividual(task, "Погода", weatherId);

        return new WeatherDTO(
            weatherId,
            helpers.getDataPropertyValue(task, weather, "Название", String.class)
        );
    }

    @Transactional(readOnly = true)
    public List<WeatherDTO> getAllWeathers(UUID taskId, UUID userId) throws OWLOntologyCreationException {

        roleRepo.findByIdUserIdAndIdTaskId(userId, taskId)
            .orElseThrow(() -> new AccessDeniedException("No access"));

        OWLOntology task = helpers.loadTaskOntology(taskId);
        return helpers.requireAllIndividuals(task, "Погода")
            .stream()
            .map(ind -> {
                return new WeatherDTO(
                    helpers.getStrIDFromURI(ind.getIRI()),
                    helpers.getDataPropertyValue(task, ind, "Текущая_погода", String.class)
                );
            })
            .toList();
    }
}
