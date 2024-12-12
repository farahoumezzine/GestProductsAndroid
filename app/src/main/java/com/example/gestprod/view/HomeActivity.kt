package com.example.gestprod.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gestprod.databinding.ActivityHomeBinding
import com.google.firebase.auth.FirebaseAuth

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Gestionnaire de clic pour ajouter un produit
        binding.addProduct.setOnClickListener {
            val intent = Intent(this, AddProductActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Gestionnaire de clic pour mettre à jour un produit
        binding.updateProduct.setOnClickListener {
            val intent = Intent(this, UpdateProductActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Gestionnaire de clic pour rechercher un produit
        binding.searchProduct.setOnClickListener {
            val intent = Intent(this, SearchProductActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Gestionnaire de clic pour voir l'inventaire des produits
        binding.viewInventory.setOnClickListener {
            val intent = Intent(this, ViewInventoryActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Gestionnaire de clic pour supprimer un produit
        binding.deleteProduct.setOnClickListener {
            val intent = Intent(this, DeleteProductActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Gestionnaire de clic pour se déconnecter
        binding.SignOut.setOnClickListener {
            FirebaseAuth.getInstance().signOut() // Déconnexion de l'utilisateur
            val intent = Intent(this@HomeActivity, SignInActivity::class.java)
            startActivity(intent) // Redirection vers l'activité de connexion
            Toast.makeText(this@HomeActivity, "Logout Successful !", Toast.LENGTH_SHORT).show() // Message de déconnexion réussie
            finish()
        }
    }
}
