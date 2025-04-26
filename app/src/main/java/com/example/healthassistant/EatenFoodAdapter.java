package com.example.healthassistant;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;  // Add Glide for image loading
import java.util.List;
import food.RecipeResponse.Recipe;


public class EatenFoodAdapter extends RecyclerView.Adapter<EatenFoodAdapter.EatenFoodViewHolder> {
    private List<Recipe> eatenList;

    public EatenFoodAdapter(List<Recipe> eatenList) {
        this.eatenList = eatenList;
    }

    @Override
    public EatenFoodViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.eaten_item, parent, false); // you create a simple layout for eaten item
        return new EatenFoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(EatenFoodViewHolder holder, int position) {
        Recipe recipe = eatenList.get(position);
        holder.titleTextView.setText(recipe.getTitle());

        // Load the image
        Glide.with(holder.itemView.getContext())
                .load(recipe.getImage())
                .into(holder.recipeImageView);

        // Optional: add click listeners for like/dislike here
        holder.likeButton.setOnClickListener(v -> {
            // Handle like
        });

        holder.dislikeButton.setOnClickListener(v -> {
            // Handle dislike
        });
    }

    @Override
    public int getItemCount() {
        return eatenList.size();
    }

    public static class EatenFoodViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        ImageView recipeImageView;
        ImageButton likeButton, dislikeButton, addedButton;
        public EatenFoodViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.recipeTitle);
            recipeImageView = itemView.findViewById(R.id.recipeImage);
            likeButton = itemView.findViewById(R.id.likeButton);
            dislikeButton = itemView.findViewById(R.id.dislikeButton);
            addedButton = itemView.findViewById(R.id.addedButton);
        }
    }
}
