package com.example.gestprod.view

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gestprod.controller.ProductsAdapter
import com.example.gestprod.databinding.ActivityViewInventoryBinding
import com.example.gestprod.model.Product

class ViewInventoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewInventoryBinding

    private var productList = mutableListOf<Product>()
    private lateinit var adapter: ProductsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflation de la vue à partir du layout ActivityViewInventoryBinding
        binding = ActivityViewInventoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configuration du RecyclerView
        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        // Initialisation de l'adaptateur avec la liste de produits
        adapter = ProductsAdapter(productList)
        binding.recyclerView.adapter = adapter

        // Chargement initial de tous les produits dans l'adaptateur
        adapter.loadAllProducts()

        // Gestionnaire de clic pour retourner à l'écran d'accueil
        binding.btnGoHome.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
