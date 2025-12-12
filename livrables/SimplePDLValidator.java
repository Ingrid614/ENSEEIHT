package simplepdl.validation;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;

import simplepdl.Guidance;
import simplepdl.Process;
import simplepdl.ProcessElement;
import simplepdl.Ressource;
import simplepdl.RessourceRequirement;
import simplepdl.SimplepdlPackage;
import simplepdl.WorkDefinition;
import simplepdl.WorkSequence;
import simplepdl.util.SimplepdlSwitch;

/**
 * Réalise la validation d'un EObject issu de SimplePDL (en théorie, d'un process).
 * Cet classe visite le modèle et utilise les caseXXX pour rediriger l'algo vers la
 * bonne méthode.
 * Attention, lorsqu'une classe est un parent il faut aller faire la visite des enfants
 * manuellement (cf. caseProcess typiquement).
 * 
 * La classe Switch exige un paramètre de généricité (et gère une partie de la visite à
 * base de comparaison à null). Ici le paramètre est un booléen mais en réalité on ne
 * s'en sert pas...
 * 
 * @author Guillaume Dupont
 * @version 0.1
 */
public class SimplePDLValidator extends SimplepdlSwitch<Boolean> {
	/**
	 * Expression régulière qui correspond à un identifiant bien formé.
	 */
	private static final String IDENT_REGEX = "^[A-Za-z_][A-Za-z0-9_]*$";
	
	/**
	 * Résultat de la validation (état interne réinitialisé à chaque nouvelle validation).
	 */
	private ValidationResult result = null;
	
	/**
	 * Construire un validateur
	 */
	public SimplePDLValidator() {}
	
	/**
	 * Lancer la validation et compiler les résultats dans un ValidationResult.
	 * Cette méthode se charge de créer un résultat de validation vide puis de
	 *  visiter les process présents dans la ressource.
	 * @param resource resource à valider
	 * @return résultat de validation
	 */
	public ValidationResult validate(Resource resource) {
		this.result = new ValidationResult();
		
		for (EObject object : resource.getContents()) {
			this.doSwitch(object);
		}
		
		return this.result;
	}


	/**
	 * Méthode appelée lorsque l'objet visité est un Process.
	 * Cet méthode amorce aussi la visite des éléments enfants.
	 * @param object élément visité
	 * @return résultat de validation (null ici, ce qui permet de poursuivre la visite
	 * vers les classes parentes, le cas échéant)
	 */
	@Override
	public Boolean caseProcess(simplepdl.Process object) {
		// Contrainte : Le nom du process respecte les conventions Java
		this.result.recordIfFailed(
			object.getName() != null && object.getName().matches(IDENT_REGEX), 
			object, 
			"Le nom du process ne respecte pas les conventions Java"
		);

		// Contrainte 4 : Noms uniques des ressources dans le process
		Set<String> ressourceNames = new HashSet<>();
		for (ProcessElement pe : object.getProcessElements()) {
			if (pe instanceof Ressource res) {
				if (!ressourceNames.add(res.getName())) {
					this.result.recordIfFailed(
						false,
						res,
						"Le nom de la ressource '" + res.getName() + "' est dupliqué dans le process '" + object.getName() + "'."
					);
				}
			}
		}

		// Visite de tous les ProcessElements
		for (ProcessElement pe : object.getProcessElements()) {
			this.doSwitch(pe);
		}

		return null;
	}


	/**
	 * Méthode appelée lorsque l'objet visité est un ProcessElement (ou un sous type).
	 * @param object élément visité
	 * @return résultat de validation (null ici, ce qui permet de poursuivre la visite
	 * vers les classes parentes, le cas échéant)
	 */
	@Override
	public Boolean caseProcessElement(ProcessElement object) {
		return null;
	}

	/**
	 * Méthode appelée lorsque l'objet visité est une WorkDefinition.
	 * @param object élément visité
	 * @return résultat de validation (null ici, ce qui permet de poursuivre la visite
	 * vers les classes parentes, le cas échéant)
	 */
	@Override
	public Boolean caseWorkDefinition(WorkDefinition object) {
		Process process = (Process) object.eContainer();

		// Contraintes sur WD
		this.result.recordIfFailed(
				object.getName() != null || object.getName().matches(IDENT_REGEX), 
				object, 
				"Le nom de l'activité ne respecte pas les conventions Java");
		
		this.result.recordIfFailed(
				process.getProcessElements().stream()
					.filter(p -> p.eClass().getClassifierID() == SimplepdlPackage.WORK_DEFINITION)
					.allMatch(pe -> (pe.equals(object) || !((WorkDefinition) pe).getName().contains(object.getName()))),
				object, 
				"Le nom de l'activité (" + object.getName() + ") n'est pas unique");
		
		// Contrainte 3 : Une ressource ne peut pas être liée plusieurs fois à une même WorkDefinition
		Set<Ressource> seenRessources = new HashSet<>();
		for (RessourceRequirement req : object.getRessourcesRequirements()) {
			if (!seenRessources.add(req.getRessource())) {
				this.result.recordIfFailed(
					false,
					object,
					"La ressource '" + req.getRessource().getName() + "' est utilisée plusieurs fois dans la WorkDefinition '" 
					+ object.getName() + "'."
				);
				break; // Inutile de continuer à chercher
			}
		}

		
		return null;
	}

	/**
	 * Méthode appelée lorsque l'objet visité est une WorkSequence.
	 * @param object élément visité
	 * @return résultat de validation (null ici, ce qui permet de poursuivre la visite
	 * vers les classes parentes, le cas échéant)
	 */
	@Override
	public Boolean caseWorkSequence(WorkSequence object) {
		WorkDefinition pred = object.getPredecessor();
		WorkDefinition succ = object.getSuccessor();

		// Vérifier que les deux sont présents
		if (pred == null || succ == null) {
			this.result.recordIfFailed(
				false,
				object,
				"La WorkSequence est incomplète : prédécesseur ou successeur manquant."
			);
			return null;
		}

		// Contrainte 1 : pas de boucle sur soi-même
		this.result.recordIfFailed(
			!pred.equals(succ),
			object,
			"La dépendance relie l'activité " + pred.getName() + " à elle-même."
		);

		// Contrainte 2 : pas de doublon exact
		Process process = (Process) object.eContainer();
		this.result.recordIfFailed(
			process.getProcessElements().stream()
				.filter(p -> p.eClass().getClassifierID() == SimplepdlPackage.WORK_SEQUENCE)
				.allMatch(pe -> (pe.equals(object)
					|| !(((WorkSequence) pe).getLinkType().equals(object.getLinkType())
					&& ((WorkSequence) pe).getPredecessor().equals(pred)
					&& ((WorkSequence) pe).getSuccessor().equals(succ)))),
			object,
			"Il existe déjà une dépendance de type " + object.getLinkType() +
			" entre " + pred.getName() + " et " + succ.getName() + "."
		);

		return null;
	}


	/**
	 * Méthode appelée lorsque l'objet visité est une Guidance.
	 * @param object élément visité
	 * @return résultat de validation (null ici, ce qui permet de poursuivre la visite
	 * vers les classes parentes, le cas échéant)
	 */
	@Override
	public Boolean caseGuidance(Guidance object) {
		// Contraintes sur la guidance : texte non vide
		this.result.recordIfFailed(
				!(object.getText() == null) , 
				object, 
				"La guidance possede un texte vide");
		return null;
	}
	/**
	 * Méthode appelée lorsque l'objet visité est une Ressource.
	 * @param object élément visité
	 * @return résultat de validation (null ici, ce qui permet de poursuivre la visite
	 * vers les classes parentes, le cas échéant)
	 */
	@Override
	public Boolean caseRessource(Ressource object) {
		// Contrainte 1 sur les ressources : Le nombre doit être >= 0
		this.result.recordIfFailed(
			object.getNumber() >= 0,
			object,
			"La ressource '" + object.getName() + "' a un nombre négatif (" + object.getNumber() + ")."
		);
		

		return null;
	}
	/**
	 * Méthode appelée lorsque l'objet visité est une RessourceRequirement.
	 * @param object élément visité
	 * @return résultat de validation (null ici, ce qui permet de poursuivre la visite
	 * vers les classes parentes, le cas échéant)
	 */
	@Override
	public Boolean caseRessourceRequirement(simplepdl.RessourceRequirement object) {
		// Contrainte 2 sur les ressources utilisées par les activités du processus : numberRequired > 0
		this.result.recordIfFailed(
			object.getNumberRequired() > 0,
			object,
			"Le nombre de ressources requises doit être strictement positif."
		);

		// Contrainte 1 : ne pas demander plus que disponible
		if (object.getRessource() != null) {
			this.result.recordIfFailed(
				object.getNumberRequired() <= object.getRessource().getNumber(),
				object,
				"La ressource requise '" + object.getRessource().getName() + "' demande " 
				+ object.getNumberRequired() + " unités, mais seulement " 
				+ object.getRessource().getNumber() + " sont disponibles."
			);
		}

		return null;
	}



	/**
	 * Cas par défaut, lorsque l'objet visité ne correspond pas à un des autres cas.
	 * Cette méthode est aussi appelée lorsqu'une méthode renvoie null (comme une sorte de
	 * fallback).
	 * On pourrait implémenter le switch différemment, en ne renvoyant null dans les autres
	 * méthodes que si la contrainte ne sert à rien, et se servir de cette méthode pour
	 * identifier les éléments étrangers (qui de toute façon ne doivent pas exister).
	 * C'est aussi la méthode appelée si on ne redéfini pas un des caseXXX.
	 * @param object objet visité
	 * @return résultat, null ici
	 */
	@Override
	public Boolean defaultCase(EObject object) {
		return null;
	}
	
	
}
