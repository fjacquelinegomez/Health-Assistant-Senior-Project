package food;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface SpoonacularService {
    @GET("recipes/complexSearch")
    Call<RecipeResponse> getRecipes(
            @Query("apiKey") String apiKey,
            @Query("number") int number,
            @Query("intolerances") String intolerances,
            @Query("diet") String diet
    );
}

