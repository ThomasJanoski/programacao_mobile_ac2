package com.ac2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.ViewHolder> {
    private static final String TAG = "MovieAdapter";
    private final List<Movie> movies;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Movie movie);

        void onItemDeleted();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public MovieAdapter(List<Movie> movies) {
        this.movies = movies;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int pos) {
        Movie movie = movies.get(pos);
        holder.txt1.setText(movie.getTitle());
        holder.txt2.setText("Year: " + movie.getYear() + " | Rating: " + movie.getRating() + "/5 | Genre: " + movie.getGenre());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(movie);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            deleteMovie(movie.getId(), holder.getAdapterPosition(), v);
            return true;
        });
    }

    private void deleteMovie(String id, int position, View view) {
        if (id == null || id.isEmpty()) {
            Toast.makeText(view.getContext(), "ID do filme inválido", Toast.LENGTH_SHORT).show();
            return;
        }
        FirebaseFirestore.getInstance().collection("movies")
                .document(id)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    movies.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, movies.size());
                    Toast.makeText(view.getContext(), "Filme deletado!", Toast.LENGTH_SHORT).show();
                    if (listener != null) {
                        listener.onItemDeleted();
                    } else {
                        Toast.makeText(view.getContext(), "Erro: Listener não definido", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(view.getContext(), "Erro ao deletar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txt1, txt2;

        public ViewHolder(View itemView) {
            super(itemView);
            txt1 = itemView.findViewById(android.R.id.text1);
            txt2 = itemView.findViewById(android.R.id.text2);
        }
    }
}