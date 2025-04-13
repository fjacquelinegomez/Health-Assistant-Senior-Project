package food;

import java.util.List;
import java.util.Set;

public class RecipeResponse {
    private List<Recipe> results;

    public List<Recipe> getResults() {
        return results;
    }

    public void setResults(List<Recipe> results) {
        this.results = results;
    }

    public static class Recipe {
        private int id;
        private String title;
        private String image;
        private Set<String> tags;  // Tags like "low-sodium", "low-carb", "peanut-free"

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }
        // Getter for tags
        public Set<String> getTags() {
            return tags;
        }

        // Setter for tags
        public void setTags(Set<String> tags) {
            this.tags = tags;
        }
    }
}

