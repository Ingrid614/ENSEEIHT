package simplepdl.topetrinet;

import java.util.HashMap;
import java.util.Map;

import petriNet1.*; // Import tout pour simplifier
import simplepdl.ProcessElement;
import simplepdl.WorkDefinition;
import simplepdl.WorkSequence;

public class SimplePDLToPetriNet {

    private Map<WorkDefinition, Place> readyPlaces = new HashMap<>();
    private Map<WorkDefinition, Place> finishedPlaces = new HashMap<>();
    private Map<WorkDefinition, Transition> workTransitions = new HashMap<>();

    public PetriNet transform(simplepdl.Process process) {
        PetriNet petriNet = PetriNet1Factory.eINSTANCE.createPetriNet();
        petriNet.setName(process.getName()); // optionnel

        for (ProcessElement element : process.getProcessElements()) {
            if (element instanceof WorkDefinition wd) {
                // Create 'ready' Place
                Place ready = PetriNet1Factory.eINSTANCE.createPlace();
                ready.setName(wd.getName() + "_ready");
                ready.setTokens(1); 
                petriNet.getPetrinetElements().add(ready);
                readyPlaces.put(wd, ready);

                // Create 'finished' Place
                Place finished = PetriNet1Factory.eINSTANCE.createPlace();
                finished.setName(wd.getName() + "_finished");
                finished.setTokens(0);
                petriNet.getPetrinetElements().add(finished);
                finishedPlaces.put(wd, finished);

                // Create Transition
                Transition transition = PetriNet1Factory.eINSTANCE.createTransition();
                transition.setName(wd.getName() + "_do");
                petriNet.getPetrinetElements().add(transition);
                workTransitions.put(wd, transition);

                // Arc from ready → transition
                Arc arcIn = PetriNet1Factory.eINSTANCE.createArc();
                arcIn.setSource(ready);
                arcIn.setTarget(transition);
                arcIn.setWeight(1);
                petriNet.getArcs().add(arcIn);

                // Arc from transition → finished
                Arc arcOut = PetriNet1Factory.eINSTANCE.createArc();
                arcOut.setSource(transition);
                arcOut.setTarget(finished);
                arcOut.setWeight(1);
                petriNet.getArcs().add(arcOut);
            }
        }

        // Handle WorkSequences
        for (ProcessElement element : process.getProcessElements()) {
            if (element instanceof WorkSequence ws) {
                WorkDefinition pred = ws.getPredecessor();
                WorkDefinition succ = ws.getSuccessor();

                Place sourcePlace = null;
                Transition targetTransition = workTransitions.get(succ);

                switch (ws.getLinkType()) {
                    case FINISH_TO_START -> sourcePlace = finishedPlaces.get(pred);
                    case START_TO_START -> sourcePlace = readyPlaces.get(pred);
                    case FINISH_TO_FINISH -> sourcePlace = finishedPlaces.get(pred);
                    case START_TO_FINISH -> sourcePlace = readyPlaces.get(pred);
                }

                if (sourcePlace != null && targetTransition != null) {
                    Arc dependencyArc = PetriNet1Factory.eINSTANCE.createArc();
                    dependencyArc.setSource(sourcePlace);
                    dependencyArc.setTarget(targetTransition);
                    dependencyArc.setWeight(1);
                    petriNet.getArcs().add(dependencyArc);
                }
            }
        }

        return petriNet;
    }
}
