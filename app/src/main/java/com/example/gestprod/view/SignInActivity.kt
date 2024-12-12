package com.example.gestprod.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gestprod.databinding.ActivitySignInBinding
import com.google.firebase.auth.FirebaseAuth

class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        GestProd
        // Initialisation de FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance()

        // Gestionnaire de clic pour rediriger vers l'écran d'inscription
        binding.tvSignUp.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        // Gestionnaire de clic pour le bouton de connexion
        binding.btnSignIn.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            // Vérification que les champs email et mot de passe ne sont pas vides
            if (email.isNotEmpty() && password.isNotEmpty()) {
                // Authentification de l'utilisateur avec Firebase
                firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Si l'authentification est réussie, redirection vers l'écran d'accueil
                            val intent = Intent(this, HomeActivity::class.java)
                            startActivity(intent)
                        } else {
                            // Affichage d'un message d'erreur en cas d'échec d'authentification
                            Toast.makeText(this, task.exception.toString(), Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
            } else {
                // Affichage d'un message si les champs email et mot de passe sont vides
                Toast.makeText(this, "Empty fields are not allowed !", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
