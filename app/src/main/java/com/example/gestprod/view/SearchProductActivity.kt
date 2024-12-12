package com.example.gestprod.view

import android.content.ContentValues.TAG
import android.content.Intent
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gestprod.R
import com.example.gestprod.controller.Controller
import com.example.gestprod.databinding.ActivitySearchProductBinding
import com.squareup.picasso.Picasso

class SearchProductActivity : AppCompatActivity() {
    private lateinit var controller: Controller

    private lateinit var binding: ActivitySearchProductBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Gestionnaire de clic pour revenir à l'écran d'accueil
        binding.btnGoHome.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        controller = Controller.getInstance()

        // Gestionnaire de clic pour rechercher un produit
        binding.btnSearch.setOnClickListener {
            val reference = binding.etReference.text.toString()

            // Appel à la méthode du contrôleur pour rechercher le produit
            controller.searchProduct(reference) { product ->
                if (product != null) {
                    // Affichage des détails du produit trouvé
                    binding.apply {
                        tvRef.text = "Reference : ${product.ref}"
                        tvPrice.text = "Price : ${product.price} DTN"
                        tvQuantity.text = "Quantity : ${product.quantity}"
                        Picasso.get().load(product.imageUrl).fit().into(ivPicture)
                    }
                } else {
                    // Aucun produit trouvé pour la référence donnée
                    Toast.makeText(applicationContext, "There is no product with this reference !", Toast.LENGTH_SHORT).show()
                    Log.e(TAG, "Product not found for reference : $reference")
                }
            }
        }
    }
}
