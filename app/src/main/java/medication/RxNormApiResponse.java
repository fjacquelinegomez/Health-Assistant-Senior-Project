package medication;

import com.example.healthassistant.Medication;

import java.util.List;

// Handles RxNorm's API response since it's a nested JSON response
// drugGroup -> ConceptGroup -> conceptProperties -> rxcui + name
public class RxNormApiResponse {
    // Takes in Drug Group part of JSON response
    private DrugGroup drugGroup;
    public DrugGroup getDrugGroup() { return drugGroup; }

    // Concept Group is nested in Drug Group
    public static class DrugGroup {
        private List<ConceptGroup> conceptGroup;
        public List<ConceptGroup> getConceptGroup() { return conceptGroup; }
    }

    // Concept Properties is nested in Concept Group
    public static class ConceptGroup {
        private List<Medication> conceptProperties;
        public List<Medication> getConceptProperties() { return conceptProperties; }
    }
}
