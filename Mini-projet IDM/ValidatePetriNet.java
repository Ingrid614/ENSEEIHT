package fr.n7.petriNet1.validation;

import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import petriNet1.PetriNet1Package;

/**
 * Réalise la validation de modèles conformes à PetriNet à l'aide du validateur et
 * affiche le résultat.
 * 
 * Les modèles sont donnés dans les arguments de la ligne de commande, et le résultat
 * est affiché dans le terminal.
 */
public class ValidatePetriNet {

    /**
     * Afficher une liste d'erreurs avec un préfixe.
     * Le préfixe est affiché avec "OK" si la liste est vide, sinon chaque erreur est affichée.
     * 
     * @param prefix préfixe à afficher avant la liste
     * @param errors erreurs à afficher
     */
    private static void afficherErreurs(String prefix, List<ValidationResult.ValidationError> errors) {
        System.out.print(prefix + ":");
        if (errors.isEmpty()) {
            System.out.println(" OK");
        } else {
            System.out.println(" " + errors.size() + " erreur(s) trouvée(s)");
            for (ValidationResult.ValidationError error : errors) {
                System.out.println("=> " + error.toString());
            }
        }
    }

    /**
     * Affiche les erreurs classées par type d'élément du modèle PetriNet.
     * 
     * @param resultat résultat de la validation
     */
    private static void afficherResultat(ValidationResult resultat) {
        afficherErreurs("- PetriNet",    resultat.getRecordedErrorsFor(petriNet1.PetriNet1Package.PETRI_NET));
        afficherErreurs("- Place",       resultat.getRecordedErrorsFor(petriNet1.PetriNet1Package.PLACE));
        afficherErreurs("- Transition",  resultat.getRecordedErrorsFor(petriNet1.PetriNet1Package.TRANSITION));
        afficherErreurs("- Arc",         resultat.getRecordedErrorsFor(petriNet1.PetriNet1Package.ARC));
    }

    /**
     * Point d'entrée principal du programme. 
     * Charge les modèles XMI passés en paramètre, les valide et affiche les résultats.
     * 
     * @param args chemins des fichiers XMI à valider
     */
    public static void main(String... args) {
        // Initialisation du package pour charger le métamodèle
        @SuppressWarnings("unused")
        PetriNet1Package packageInstance = PetriNet1Package.eINSTANCE;

        // Enregistrement du factory XMI pour les fichiers .xmi
        Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
        Map<String, Object> m = reg.getExtensionToFactoryMap();
        m.put("xmi", new XMIResourceFactoryImpl());

        // Création du resourceSet
        ResourceSet resSet = new ResourceSetImpl();

        // Instanciation du validateur
        PetriNetValidator validator = new PetriNetValidator();

        // Traitement de chaque fichier passé en argument
        for (String model : args) {
            URI modelURI = URI.createURI(model);
            Resource resource = resSet.getResource(modelURI, true);
            ValidationResult resultat = validator.validate(resource);

            System.out.println("\nRésultat de validation pour : " + model);
            afficherResultat(resultat);
        }

        System.out.println("\nValidation terminée.");
    }
}
