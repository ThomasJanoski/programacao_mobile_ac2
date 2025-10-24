package com.ac2;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText etTitle, etDirector, etYear;
    private RatingBar rbRating;
    private Spinner spGenre, spFilterGenre, spSort;
    private CheckBox cbSeenInCinema, cbFilterCinema;
    private Button btnSave;
    private CollectionReference dbRef;
    private Movie selectedMovie = null;
    private String[] movieGenres;
    private final List<String> filterGenres = new ArrayList<>();
    private final List<Movie> moviesList = new ArrayList<>();
    private MovieAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firestore
        dbRef = FirebaseFirestore.getInstance().collection("movies");

        // Initialize views
        etTitle = findViewById(R.id.et_title);
        etDirector = findViewById(R.id.et_director);
        etYear = findViewById(R.id.et_year);
        rbRating = findViewById(R.id.rb_rating);
        spGenre = findViewById(R.id.sp_genre);
        cbSeenInCinema = findViewById(R.id.cb_seen_in_cinema);
        btnSave = findViewById(R.id.btn_save);
        spFilterGenre = findViewById(R.id.sp_filter_genre);
        cbFilterCinema = findViewById(R.id.cb_filter_cinema);
        spSort = findViewById(R.id.sp_sort);
        RecyclerView rvMovies = findViewById(R.id.rv_movies);

        // Initialize RecyclerView
        adapter = new MovieAdapter(moviesList);
        rvMovies.setLayoutManager(new LinearLayoutManager(this));
        rvMovies.setAdapter(adapter);
        adapter.setOnItemClickListener(new MovieAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Movie movie) {
                selectedMovie = movie;
                loadMovieToFields(movie);
                btnSave.setText("Atualizar filme");
            }

            @Override
            public void onItemDeleted() {
                clearFields();
                loadMovies();
            }
        });

        movieGenres = getResources().getStringArray(R.array.movie_genres);
        filterGenres.add("Todos os gêneros");
        filterGenres.addAll(Arrays.asList(movieGenres));
        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, filterGenres);
        spFilterGenre.setAdapter(filterAdapter);

        btnSave.setOnClickListener(v -> {
            saveMovie();
            btnSave.setText("Salvar");
        });

        // Filter by genre
        spFilterGenre.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                refreshList();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        cbFilterCinema.setOnCheckedChangeListener((buttonView, isChecked) -> refreshList());

        // Sort by rating or year
        spSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                refreshList();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        loadMovies();
    }

    private void saveMovie() {
        String title = etTitle.getText().toString().trim();
        String director = etDirector.getText().toString().trim();
        String yearStr = etYear.getText().toString().trim();
        int rating = (int) rbRating.getRating();
        String genre = spGenre.getSelectedItem().toString();
        boolean seenInCinema = cbSeenInCinema.isChecked();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(director) || TextUtils.isEmpty(yearStr)) {
            Toast.makeText(this, "Por favor, preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }

        int year;
        try {
            year = Integer.parseInt(yearStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Formato de ano inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedMovie == null) {
            Movie movie = new Movie(null, title, director, year, rating, genre, seenInCinema);
            dbRef.add(movie)
                    .addOnSuccessListener(doc -> {
                        movie.setId(doc.getId());
                        Toast.makeText(this, "Filme salvo!", Toast.LENGTH_SHORT).show();
                        clearFields();
                        loadMovies();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Inserção de filme falhou: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            selectedMovie.setTitle(title);
            selectedMovie.setDirector(director);
            selectedMovie.setYear(year);
            selectedMovie.setRating(rating);
            selectedMovie.setGenre(genre);
            selectedMovie.setSeenInCinema(seenInCinema);
            dbRef.document(selectedMovie.getId())
                    .set(selectedMovie)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Filme atualizado!", Toast.LENGTH_SHORT).show();
                        clearFields();
                        loadMovies();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Atualização de filme falhou: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void loadMovies() {
        dbRef.get()
                .addOnSuccessListener(query -> {
                    moviesList.clear();
                    for (QueryDocumentSnapshot doc : query) {
                        Movie movie = doc.toObject(Movie.class);
                        movie.setId(doc.getId());
                        moviesList.add(movie);
                    }
                    adapter.notifyDataSetChanged();
                    refreshList();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Caregamento de filme falhou: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void loadMovieToFields(Movie movie) {
        etTitle.setText(movie.getTitle());
        etDirector.setText(movie.getDirector());
        etYear.setText(String.valueOf(movie.getYear()));
        rbRating.setRating(movie.getRating());

        int genreIndex = Arrays.asList(movieGenres).indexOf(movie.getGenre());
        spGenre.setSelection(Math.max(genreIndex, 0));
        cbSeenInCinema.setChecked(movie.isSeenInCinema());
    }

    private void clearFields() {
        etTitle.setText("");
        etDirector.setText("");
        etYear.setText("");
        rbRating.setRating(0f);
        spGenre.setSelection(0);
        cbSeenInCinema.setChecked(false);
        selectedMovie = null;
        btnSave.setText("Salvar");
    }

    private void refreshList() {
        String filterGenre = spFilterGenre.getSelectedItem().toString();
        boolean filterCinema = cbFilterCinema.isChecked();
        String sortBy = spSort.getSelectedItem().toString();

        loadMovies();

        List<Movie> filteredMovies = new ArrayList<>();
        for (Movie movie : moviesList) {
            boolean matchesGenre = filterGenre.equals("Todos os gêneros") || movie.getGenre().equals(filterGenre);
            boolean matchesCinema = !filterCinema || movie.isSeenInCinema();
            if (matchesGenre && matchesCinema) {
                filteredMovies.add(movie);
            }
        }

        if (sortBy.equals("Nota - Desc")) {
            Collections.sort(filteredMovies, (m1, m2) -> Integer.compare(m2.getRating(), m1.getRating()));
        } else if (sortBy.equals("Ano - Asc")) {
            Collections.sort(filteredMovies, Comparator.comparingInt(Movie::getYear));
        }

        moviesList.clear();
        moviesList.addAll(filteredMovies);
        adapter.notifyDataSetChanged();
    }
}