package simplepdl.topetrinet;

import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.Resource.Factory;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import petriNet1.*;
import simplepdl.*;
import simplepdl.Process;

public class TestTransform {
    public static void main(String[] args) {
    	// Enregistrement du package SimplePDL
    	SimplepdlPackage packageInstance = SimplepdlPackage.eINSTANCE;
    	EPackage.Registry.INSTANCE.put("http://simplepdl", packageInstance);
    	
        // Enregistrement de l'extension .xmi
        Factory.Registry reg = Factory.Registry.INSTANCE;
        Map<String, Object> m = reg.getExtensionToFactoryMap();
        m.put("xmi", new XMIResourceFactoryImpl());

        System.out.println("=== Transformation d'un processus créé en mémoire ===");

        // Création du modèle SimplePDL en mémoire
        Process process = SimplepdlFactory.eINSTANCE.createProcess();
        process.setName("ExampleProcess");

        WorkDefinition wd1 = SimplepdlFactory.eINSTANCE.createWorkDefinition();
        wd1.setName("TaskA");
        process.getProcessElements().add(wd1);

        WorkDefinition wd2 = SimplepdlFactory.eINSTANCE.createWorkDefinition();
        wd2.setName("TaskB");
        process.getProcessElements().add(wd2);

        WorkSequence ws = SimplepdlFactory.eINSTANCE.createWorkSequence();
        ws.setLinkType(WorkSequenceType.FINISH_TO_START);
        ws.setPredecessor(wd1);
        ws.setSuccessor(wd2);
        process.getProcessElements().add(ws);

        // Transformation
        SimplePDLToPetriNet transformer = new SimplePDLToPetriNet();
        PetriNet petriNet = transformer.transform(process);
        
        // Enregistrement du package PetriNet
        PetriNet1Package petriNetPackage = PetriNet1Package.eINSTANCE;
        EPackage.Registry.INSTANCE.put("http://petriNet1", petriNetPackage);

        // Création d’un ResourceSet et d’une ressource pour sauvegarder le modèle
        ResourceSet resSet2 = new org.eclipse.emf.ecore.resource.impl.ResourceSetImpl();
        Resource.Factory.Registry reg2 = Resource.Factory.Registry.INSTANCE;
        Map<String, Object> m2 = reg2.getExtensionToFactoryMap();
        m2.put("xmi", new XMIResourceFactoryImpl());

        // Chemin de sortie du modèle PetriNet généré
        URI outputURI = URI.createFileURI("models/GeneratedPetriNet.xmi");

        // Création et sauvegarde de la ressource
        Resource petriResource = resSet2.createResource(outputURI);
        petriResource.getContents().add(petriNet);

        try {
            petriResource.save(null);
            System.out.println("✅ Modèle PetriNet sauvegardé dans : " + outputURI.toFileString());
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la sauvegarde du modèle PetriNet : " + e.getMessage());
            e.printStackTrace();
        }


        // Affichage
        printPetriNet(petriNet);

        System.out.println("\n=== Transformation d'un processus existant (fichier XMI) ===");

        try {
            URI modelURI = URI.createURI("models/SimplePDLCreator_Created_Process.xmi");
            ResourceSet resSet = new org.eclipse.emf.ecore.resource.impl.ResourceSetImpl();
            Resource resource = resSet.getResource(modelURI, true);

            Process loadedProcess = (Process) resource.getContents().get(0);

            PetriNet transformedPetriNet = transformer.transform(loadedProcess);

            printPetriNet(transformedPetriNet);

        } catch (Exception e) {
            System.err.println("Erreur lors du chargement ou de la transformation du modèle : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void printPetriNet(PetriNet petriNet) {
        System.out.println("Réseau de Pétri : " + petriNet.getName());

        System.out.println("Places :");
        for (Node node : petriNet.getPetrinetElements()) {
            if (node instanceof Place place) {
                System.out.println("  " + place.getName() + " (tokens=" + place.getTokens() + ")");
            }
        }

        System.out.println("Transitions :");
        for (Node node : petriNet.getPetrinetElements()) {
            if (node instanceof Transition transition) {
                System.out.println("  " + transition.getName());
            }
        }

        System.out.println("Arcs :");
        for (Arc arc : petriNet.getArcs()) {
            String sourceName = arc.getSource() instanceof Place
                    ? ((Place) arc.getSource()).getName()
                    : ((Transition) arc.getSource()).getName();
            String targetName = arc.getTarget() instanceof Place
                    ? ((Place) arc.getTarget()).getName()
                    : ((Transition) arc.getTarget()).getName();

            System.out.println("  " + sourceName + " -> " + targetName + " (weight=" + arc.getWeight() + ")");
        }
    }
}
