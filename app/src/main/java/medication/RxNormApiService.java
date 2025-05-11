package medication;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

// Setting up the RxNormAPi endpoint(s) in Retrofit
public interface RxNormApiService {
    // Requests for medication information from RxNormApi
    @GET("drugs.json")
    Call<RxNormApiResponse> searchDrugs(@Query("name") String drugName);

    // Request for generic medication name using RxCUI
    @GET("rxcui/{rxcui}/properties.json")
    Call<RxNormApiNameResponse> fetchAllNames(@Path("rxcui") String rxcui);
}
