package com.example.healthassistant;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;


import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;  // Add Glide for image loading

import java.util.ArrayList;
import java.util.List;

import food.RecipeResponse.Recipe;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    private List<Recipe> recipeList;
    private List<Recipe> likedRecipes = new ArrayList<>();
    private List<Recipe> dislikedRecipes = new ArrayList<>();


    public RecipeAdapter(List<Recipe> recipeList) {
        this.recipeList = recipeList;
    }

    @Override
    public RecipeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recipe_item, parent, false);  // Create a layout for each item
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecipeViewHolder holder, int position) {
        Recipe recipe = recipeList.get(position);
        holder.titleTextView.setText(recipe.getTitle());

        // Use Glide to load the image from the URL
        Glide.with(holder.itemView.getContext())
                .load(recipe.getImage())
                .into(holder.recipeImageView);

        // new
        holder.addButton.setOnClickListener(v -> {
            if (addClickListener != null) {
                addClickListener.onAddClick(recipe);
            }
        });

        holder.likeButton.setOnClickListener(v -> {
            likedRecipes.add(recipe);

            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("users")
                    .document(userId)
                    .collection("likedRecipes")
                    .document(String.valueOf(recipe.getId()))  // assuming Recipe has getId()
                    .set(recipe)  // if Recipe is a valid model
                    .addOnSuccessListener(aVoid -> Toast.makeText(v.getContext(), "Liked " + recipe.getTitle(), Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(v.getContext(), "Error saving like", Toast.LENGTH_SHORT).show());
        });

        holder.dislikeButton.setOnClickListener(v -> {dislikedRecipes.add(recipe);
            dislikedRecipes.add(recipe);
            recipeList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, recipeList.size());

            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("users")
                    .document(userId)
                    .collection("dislikedRecipes")
                    .document(String.valueOf(recipe.getId()))
                    .set(recipe)
                    .addOnSuccessListener(aVoid -> Toast.makeText(v.getContext(), "Disliked " + recipe.getTitle(), Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(v.getContext(), "Error saving dislike", Toast.LENGTH_SHORT).show());
        });

    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    public static class RecipeViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        ImageView recipeImageView;
        ImageButton addButton;  // <- NEW

        ImageButton likeButton;
        ImageButton dislikeButton;


        public RecipeViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.recipeTitle);
            recipeImageView = itemView.findViewById(R.id.recipeImage);
            addButton = itemView.findViewById(R.id.addButton); // <- NEW
            likeButton = itemView.findViewById(R.id.likeButtonRecommended);
            dislikeButton = itemView.findViewById(R.id.dislikeButtonRecommended);

        }


    }

    // New, for adding foods to eaten
    public interface OnAddClickListener {
        void onAddClick(Recipe recipe);
    }

    private OnAddClickListener addClickListener;

    public void setOnAddClickListener(OnAddClickListener listener) {
        this.addClickListener = listener;
    }

    // New, for removing from recs when added
    public void removeRecipe(Recipe recipe) {
        recipeList.remove(recipe);
        notifyDataSetChanged();
    }
}
