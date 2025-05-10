package medication;

import com.google.gson.annotations.SerializedName;

import java.util.List;

// Handles OpenFDA's API response since it's nested under the results key
// Results -> warnings
public class OpenFDAApiResponse {
    // Takes in results part of JSON response
    @SerializedName("results")
    private List<DrugLabel> results;
    public List<DrugLabel> getResults() {
        return results;
    }

    // Pulls the relevant information in the properties part of the JSON response
    public static class DrugLabel {
        @SerializedName("warnings")
        private List<String> warnings;

        public List<String> getWarnings() {
            return warnings;
        }
    }
}
