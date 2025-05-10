package medication;

import java.util.List;

// Handles RxNorm's API response since it's nested under the properties key
// Properties -> name + synonym (alternate name of medication)
public class RxNormApiNameResponse {
    // Takes in properties part of JSON response
    private Properties properties;
    public Properties getProperties() {
        return properties;
    }

    // Pulls the relevant information in the properties part of the JSON response
    public static class Properties {
        private String name;
        private String synonym;

        public String getName() {
            return name;
        }
        public String getSynonym() {
            return synonym;
        }
    }
}
