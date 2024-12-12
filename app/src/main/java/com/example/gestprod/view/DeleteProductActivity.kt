package com.example.gestprod.view

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gestprod.controller.Controller
import com.example.gestprod.databinding.ActivityDeleteProductBinding

class DeleteProductActivity : AppCompatActivity() {
    private lateinit var controller: Controller
    private lateinit var binding: ActivityDeleteProductBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeleteProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        controller = Controller.getInstance()

        binding.btnGoHome.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.btnDelete.setOnClickListener{
            val reference = binding.etReference.text.toString()

            // Vérifie que la référence n'est pas vide avant de procéder à la suppression
            if (reference.isNotEmpty()) {
                AlertDialog.Builder(this@DeleteProductActivity) // Utilisation de this@DeleteProductActivity pour le contexte
                    .setTitle("Delete Product")
                    .setMessage("Are you sure you want to delete this product?")
                    .setPositiveButton("Yes") { _, _ ->
                        // L'utilisateur a confirmé, suppression du produit
                        controller.deleteProduct(reference, this@DeleteProductActivity)
                    }
                    .setNegativeButton("No", null)
                    .show()
            } else {
                Toast.makeText(this@DeleteProductActivity, "Reference cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
