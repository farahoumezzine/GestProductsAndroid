package com.example.gestprod.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gestprod.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialisation du binding pour lier la vue
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialisation de FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance()

        // Gestionnaire de clic pour rediriger vers l'écran de connexion
        binding.tvSignIn.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }

        // Gestionnaire de clic pour le bouton de création de compte
        binding.btnSignUp.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()

            // Vérification que les champs email, mot de passe et confirmation de mot de passe ne sont pas vides
            if (email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()) {
                // Vérification que les mots de passe correspondent
                if (password == confirmPassword) {
                    // Création du compte utilisateur avec Firebase
                    firebaseAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Si la création du compte est réussie, redirection vers l'écran de connexion
                                val intent = Intent(this, SignInActivity::class.java)
                                startActivity(intent)
                            } else {
                                // Affichage d'un message d'erreur en cas d'échec de création du compte
                                Toast.makeText(this, task.exception.toString(), Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                } else {
                    // Affichage d'un message si les mots de passe ne correspondent pas
                    Toast.makeText(this, "Password is not matching", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Affichage d'un message si les champs email, mot de passe ou confirmation de mot de passe sont vides
                Toast.makeText(this, "Empty fields are not allowed!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}