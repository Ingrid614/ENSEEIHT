package fr.n7.petriNet1.validation;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;

import petriNet1.Arc;
import petriNet1.Node;
import petriNet1.PetriNet;
import petriNet1.Place;
import petriNet1.Transition;

/**
 * Classe de validation pour le métamodèle PetriNet qui vérifie les contraintes sur les objets du modèle.
 */
public class PetriNetValidator extends petriNet1.util.PetriNet1Switch<Boolean> {

    private ValidationResult result = null;

    
    public PetriNetValidator() {}

    /**
     * Lancer la validation d'une ressource EMF contenant des éléments du modèle PetriNet.
     * 
     * @param resource La ressource à valider
     * @return un objet contenant les résultats de la validation
     */
    public ValidationResult validate(Resource resource) {
        this.result = new ValidationResult();

        for (EObject object : resource.getContents()) {
            this.doSwitch(object);
        }

        return this.result;
    }

    
    @Override
    public Boolean casePetriNet(PetriNet object) {
        // Contrainte : unicité des noms de places et transitions
        Set<String> placeNames = new HashSet<>();
        Set<String> transitionNames = new HashSet<>();

        for (Node node : object.getPetrinetElements()) {
            if (node instanceof Place) {
                Place place = (Place) node;
                if (!placeNames.add(place.getName())) {
                    this.result.recordIfFailed(
                        false,
                        place,
                        "Le nom de la place '" + place.getName() + "' est dupliqué."
                    );
                }
                this.doSwitch(place); // appel à casePlace
            } else if (node instanceof Transition) {
                Transition transition = (Transition) node;
                if (!transitionNames.add(transition.getName())) {
                    this.result.recordIfFailed(
                        false,
                        transition,
                        "Le nom de la transition '" + transition.getName() + "' est dupliqué."
                    );
                }
                this.doSwitch(transition); // appel à caseTransition
            } else {
                this.result.recordIfFailed(
                    false,
                    node,
                    "Type de noeud inconnu : " + node.getClass().getSimpleName()
                );
            }
        }

        // Valider tous les arcs
        for (Arc arc : object.getArcs()) {
            this.doSwitch(arc); // appel à caseArc
        }

        return null;
    }


    
    @Override
    public Boolean casePlace(Place object) {
        Integer tokens = object.getTokens();
        this.result.recordIfFailed(
            tokens != null && tokens >= 0,
            object,
            "Le nombre de jetons pour la place '" + object.getName() + "' ne peut pas être nul ou négatif."
        );
        return null;
    }


    
    @Override
    public Boolean caseTransition(Transition object) {
        this.result.recordIfFailed(
            object.getName() != null && !object.getName().trim().isEmpty(),
            object,
            "Le nom de la transition ne peut pas être vide."
        );
        return null;
    }

   
    @Override
    public Boolean caseArc(Arc object) {
        // Poids ≥ 1
    	Integer weight = object.getWeight();
    	this.result.recordIfFailed(
    	    weight != null && weight >= 1,
    	    object,
    	    "Le poids de l'arc doit être défini et ≥ 1."
    	);
        // Source et cible de types différents
        boolean bothPlace = object.getSource() instanceof Place && object.getTarget() instanceof Place;
        boolean bothTransition = object.getSource() instanceof Transition && object.getTarget() instanceof Transition;

        this.result.recordIfFailed(
            !(bothPlace || bothTransition),
            object,
            "Un arc ne peut pas relier deux éléments du même type (Place→Place ou Transition→Transition)."
        );

        return null;
    }

    
    @Override
    public Boolean defaultCase(EObject object) {
        return null;
    }
}
