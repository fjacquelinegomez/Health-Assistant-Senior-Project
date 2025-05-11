package medication;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface OpenFDAApiService {
    // Request for medication's warning
    @GET("drug/label.json")
    Call<OpenFDAApiResponse> getDrugWarnings(@Query("search") String search, @Query("limit") int limit);
}
