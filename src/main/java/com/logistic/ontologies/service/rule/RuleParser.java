package com.logistic.ontologies.service.rule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.SWRLArgument;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLBuiltInAtom;
import org.semanticweb.owlapi.model.SWRLClassAtom;
import org.semanticweb.owlapi.model.SWRLDArgument;
import org.semanticweb.owlapi.model.SWRLDataPropertyAtom;
import org.semanticweb.owlapi.model.SWRLLiteralArgument;
import org.semanticweb.owlapi.model.SWRLObjectPropertyAtom;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.model.SWRLVariable;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.semanticweb.owlapi.vocab.SWRLBuiltInsVocabulary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.logistic.ontologies.dto.rule.Argument;
import com.logistic.ontologies.dto.rule.Atom;
import com.logistic.ontologies.dto.rule.BuiltInAtom;
import com.logistic.ontologies.dto.rule.ClassAtom;
import com.logistic.ontologies.dto.rule.DataPropertyAtom;
import com.logistic.ontologies.dto.rule.ObjectPropertyAtom;
import com.logistic.ontologies.dto.rule.RuleCreateDTO;
import com.logistic.ontologies.dto.rule.RuleDTO;
import com.logistic.ontologies.service.ontology.OntologyStorage;

import openllet.owlapi.SWRL;
import uk.ac.manchester.cs.owl.owlapi.SWRLVariableImpl;

@Component
public class RuleParser {

    @Autowired
    protected OntologyStorage ontologyStorage;

    public SWRLRule toSwrlRule(RuleCreateDTO ruleDTO, OWLOntology ontology) {
        Set<SWRLAtom> body = toSwrlAtoms(ruleDTO.antecedent(), ontology);
        Set<SWRLAtom> head = toSwrlAtoms(ruleDTO.consequent(), ontology);

        return SWRL.rule(body, head);
    }

    public RuleDTO fromSwrlRule(SWRLRule swrlRule, OWLOntology ontology, UUID ruleOntologyId, UUID ruleId) {
        Map<String, String> varTypes = new HashMap<>();

        List<Atom> antecedent = mapAtoms(swrlRule.getBody(), ontology, varTypes);
        List<Atom> consequent = mapAtoms(swrlRule.getHead(), ontology, varTypes);

        return new RuleDTO(ruleOntologyId, ruleId, antecedent, consequent);
    }

    private void enrichVarTypes(SWRLAtom atom, OWLOntology ontology, Map<String, String> varTypes) {
        if (atom instanceof SWRLClassAtom classAtom) {
            String type = classAtom.getPredicate().asOWLClass().getIRI().getFragment();
            SWRLVariable var =  (SWRLVariable) ((ArrayList<SWRLArgument>) classAtom.getAllArguments()).get(0);

            varTypes.putIfAbsent("?" + var.getIRI().getFragment(), type);
        } if (atom instanceof SWRLObjectPropertyAtom objAtom) {
            OWLClassExpression domain = ontology.importsClosure()
                .flatMap(o -> EntitySearcher.getDomains(objAtom.getPredicate(), o))
                .findFirst()
                .orElse(null);
            OWLClassExpression range = ontology.importsClosure()
                .flatMap(o -> EntitySearcher.getRanges(objAtom.getPredicate(), o))
                .findFirst()
                .orElse(null);

            SWRLVariable sub = (SWRLVariable) ((ArrayList<SWRLArgument>) objAtom.getAllArguments()).get(0);
            SWRLVariable obj = (SWRLVariable) ((ArrayList<SWRLArgument>) objAtom.getAllArguments()).get(1);

            varTypes.putIfAbsent("?" + sub.getIRI().getFragment(), domain.asOWLClass().getIRI().getFragment());
            varTypes.putIfAbsent("?" + obj.getIRI().getFragment(), range.asOWLClass().getIRI().getFragment());
        } if (atom instanceof SWRLDataPropertyAtom dataAtom) {
            SWRLVariable sub = (SWRLVariable) ((ArrayList<SWRLArgument>) dataAtom.getAllArguments()).get(0);
            SWRLDArgument val = (SWRLDArgument) ((ArrayList<SWRLArgument>) dataAtom.getAllArguments()).get(1);

            OWLClassExpression domain = ontology.importsClosure()
                .flatMap(o -> EntitySearcher.getDomains((OWLDataProperty) dataAtom.getPredicate(), o))
                .findFirst()
                .orElse(null);
            OWLDataRange range = ontology.importsClosure()
                .flatMap(o -> EntitySearcher.getRanges((OWLDataProperty) dataAtom.getPredicate(), o))
                .findFirst()
                .orElse(null);

            varTypes.putIfAbsent("?" + sub.getIRI().getFragment(), domain.asOWLClass().getIRI().getFragment());

            varTypes.putIfAbsent(
                val instanceof SWRLLiteralArgument litVAl ? litVAl.getLiteral().getLiteral() : "?" + ((SWRLVariableImpl) val).getIRI().getFragment(),
                range.getDataRangeType().getIRI().getFragment()
            );
        }
    }

    private List<Atom> mapAtoms(Set<SWRLAtom> atoms, OWLOntology ontology, Map<String, String> varTypes) {
        atoms.forEach(atom -> enrichVarTypes(atom, ontology, varTypes));
        return atoms.stream()
            .map(atom -> mapSingleAtom(atom, ontology, varTypes))
            .toList();
    }

    private Atom mapSingleAtom(SWRLAtom atom, OWLOntology ontology, Map<String, String> varTypes) {
        if (atom instanceof SWRLClassAtom classAtom) {
            String predicate = classAtom.getPredicate().asOWLClass().getIRI().getFragment();

            SWRLVariable var = (SWRLVariable) ((ArrayList<SWRLArgument>) classAtom.getAllArguments()).get(0);

            Argument arg = new Argument(predicate, "?" + var.getIRI().getFragment());

            return new ClassAtom(predicate, arg);
        } if (atom instanceof SWRLObjectPropertyAtom objAtom) {
            String predicate = ((OWLObjectProperty) objAtom.getPredicate()).getIRI().getFragment();

            SWRLVariable sub = (SWRLVariable) ((ArrayList<SWRLArgument>) objAtom.getAllArguments()).get(0);
            SWRLVariable obj = (SWRLVariable) ((ArrayList<SWRLArgument>) objAtom.getAllArguments()).get(1);

            OWLClassExpression domain = ontology.importsClosure()
                .flatMap(o -> EntitySearcher.getDomains(objAtom.getPredicate(), o))
                .findFirst()
                .orElse(null);
            OWLClassExpression range = ontology.importsClosure()
                .flatMap(o -> EntitySearcher.getRanges(objAtom.getPredicate(), o))
                .findFirst()
                .orElse(null);

            Argument subject = new Argument(domain.asOWLClass().getIRI().getFragment(), "?" + sub.getIRI().getFragment());
            Argument object = new Argument(range.asOWLClass().getIRI().getFragment(), "?" + obj.getIRI().getFragment());

            return new ObjectPropertyAtom(predicate, subject, object);
        } if (atom instanceof SWRLDataPropertyAtom dataAtom) {
            String predicate = ((OWLDataProperty) dataAtom.getPredicate()).getIRI().getFragment();

            SWRLVariable sub = (SWRLVariable) ((ArrayList<SWRLArgument>) dataAtom.getAllArguments()).get(0);
            SWRLDArgument val = (SWRLDArgument) ((ArrayList<SWRLArgument>) dataAtom.getAllArguments()).get(1);

            OWLClassExpression domain = ontology.importsClosure()
                .flatMap(o -> EntitySearcher.getDomains((OWLDataProperty) dataAtom.getPredicate(), o))
                .findFirst()
                .orElse(null);

            Argument subject = new Argument(domain.asOWLClass().getIRI().getFragment(), "?" + sub.getIRI().getFragment());
            Argument value = mapArgument(val, varTypes);

            return new DataPropertyAtom(predicate, subject, value);
        } if (atom instanceof SWRLBuiltInAtom builtIn) {
            String predicate = builtIn.getPredicate().getFragment();

            List<Argument> args = builtIn.getAllArguments().stream()
                .map(arg -> mapArgument(arg, varTypes))
                .toList();

            return new BuiltInAtom(predicate, args);
        }
        throw new IllegalArgumentException("Unknown atom: " + atom);
    }

    private Argument mapArgument(SWRLArgument arg, Map<String, String> varTypes) {
        if (arg instanceof SWRLVariable var) {
            String name = "?" + var.getIRI().getFragment();
            return new Argument(varTypes.get(name), name);
        } if (arg instanceof SWRLLiteralArgument lit) {
            return new Argument(
                lit.getLiteral().getDatatype().getIRI().getFragment(),
                lit.getLiteral().getLiteral()
            );
        }
        throw new IllegalArgumentException("Unknown argument type: " + arg.getClass());
    }

    public Set<SWRLAtom> toSwrlAtoms(List<Atom> atoms, OWLOntology ontology) {
        OWLDataFactory factory = ontology.getOWLOntologyManager().getOWLDataFactory();
        return atoms.stream()
            .map(atom -> mapAtom(atom, factory))
            .collect(Collectors.toSet());
    }

    private SWRLAtom mapAtom(Atom atom, OWLDataFactory factory) {
        if (atom instanceof ClassAtom classAtom) {
            OWLClass owlClass = ontologyStorage.getClassByName(classAtom.className());

            SWRLVariable var = variable(classAtom.variable());

            return SWRL.classAtom(owlClass, var);
        } if (atom instanceof DataPropertyAtom dataAtom) {
            OWLDataProperty property = ontologyStorage.getDataPropertyByName(dataAtom.dataPropertyName());

            SWRLVariable subject = variable(dataAtom.subject());
            SWRLDArgument value = dataArgument(dataAtom.value());

            return SWRL.propertyAtom(property, subject, value);
        } if (atom instanceof ObjectPropertyAtom objAtom) {
            OWLObjectProperty property = ontologyStorage.getObjectPropertyByName(objAtom.objectPropertyName());

            SWRLVariable subject = variable(objAtom.subject());
            SWRLVariable object = variable(objAtom.object());

            return SWRL.propertyAtom(property, subject, object);
        } if (atom instanceof BuiltInAtom builtInAtom) {
            SWRLBuiltInsVocabulary builtIn =
                ontologyStorage.getSWRLBuiltInByName(builtInAtom.builtInFunction());

            List<SWRLDArgument> args = builtInAtom.arguments().stream()
                .map(this::dataArgument)
                .toList();

            return SWRL.builtIn(builtIn, args);
        }
        throw new IllegalArgumentException("Unknown atom type: " + atom.getClass());
    }

    private SWRLVariable variable(Argument arg) {
        if (!arg.value().startsWith("?"))
            throw new IllegalArgumentException("Expected variable, got: " + arg.value());
        return SWRL.variable(IRI.create("urn:swrl:var#" + arg.value().substring(1)));
    }

    private SWRLDArgument dataArgument(Argument arg) {
        if (arg.value().startsWith("?")) {
            return variable(arg);
        }
        // if (arg.type() != null && arg.type().startsWith("xsd:")) {
        if (arg.type() != null && arg.type().matches("int|integer|float|double|decimal|long|short|byte")) {
            double val = Double.parseDouble(arg.value());
            return SWRL.constant(val);
        }
        return SWRL.constant(arg.value());
    }
}
