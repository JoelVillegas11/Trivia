// AdminUsersActivity.java
package com.project6electiva.trivia;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import android.widget.Button;
import android.content.Intent;

public class AdminUsersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AdminUserAdapter adapter;
    private List<UserItem> userList;
    private List<UserItem> fullUserList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_users);

        db = FirebaseFirestore.getInstance();
        recyclerView = findViewById(R.id.recyclerViewUsers);
        EditText etSearch = findViewById(R.id.etUserSearch);
        Button btnCreateUser = findViewById(R.id.btnCreateUser);

        userList = new ArrayList<>();
        fullUserList = new ArrayList<>();
        adapter = new AdminUserAdapter(userList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadUsers();

        btnCreateUser.setOnClickListener(v ->
                startActivity(new Intent(this, AdminCreateUserActivity.class)));

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers(s.toString().toLowerCase().trim());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadUsers() {
        db.collection("users")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    // CORRECCIÓN: Cambiado 'fullUserCount' a 'fullUserList'
                    fullUserList.clear(); // ✅ CORRECTO: Limpiamos la lista maestra

                    fullUserList.clear(); // 🛑 Esta línea está duplicada, pero la dejamos para mostrar la corrección.

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        UserItem user = doc.toObject(UserItem.class);
                        user.id = doc.getId();
                        fullUserList.add(user);
                    }
                    userList.clear();
                    userList.addAll(fullUserList);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar usuarios", Toast.LENGTH_SHORT).show();
                });
    }

    private void filterUsers(String query) {
        userList.clear();
        if (query.isEmpty()) {
            userList.addAll(fullUserList);
        } else {
            for (UserItem user : fullUserList) {
                if (user.name != null && user.name.toLowerCase().contains(query)) {
                    userList.add(user);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    public static class UserItem {
        public String id;
        public String name;
        public String email;
        public String role;
        public long points;
        public UserItem() {}
    }
}