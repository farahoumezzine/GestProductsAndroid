package com.example.gestprod.controller

import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.gestprod.databinding.ProductViewBinding
import com.example.gestprod.model.Product
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class ProductsAdapter(private val productList: MutableList<Product>) : RecyclerView.Adapter<ProductsAdapter.ProductViewHolder>() {

    private lateinit var firebaseFirestore: FirebaseFirestore

    // Création d'un ViewHolder lorsqu'il n'y a pas de ViewHolder recyclé à utiliser
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ProductViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    // Liaison des données d'un produit avec un ViewHolder spécifique
    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]
        holder.bind(product)
    }

    // Retourne le nombre total d'articles dans la liste
    override fun getItemCount(): Int {
        return productList.size
    }

    // Charge tous les produits depuis Firestore
    fun loadAllProducts() {
        firebaseFirestore = FirebaseFirestore.getInstance()

        firebaseFirestore.collection("Products")
            .get()
            .addOnSuccessListener { querySnapshot ->
                productList.clear() // Efface la liste actuelle de produits
                for (document in querySnapshot.documents) {
                    val reference = document.getString("reference") ?: "" // Récupère la référence du produit
                    val price = document.getDouble("price") ?: 0.0 // Récupère le prix du produit
                    val quantity = document.getLong("quantity") ?: 0 // Récupère la quantité du produit
                    val pictureUrl = document.getString("picture") ?: "" // Récupère l'URL de l'image du produit

                    val product = Product(reference, price, quantity, pictureUrl) // Crée un objet Product avec les données récupérées
                    productList.add(product) // Ajoute le produit à la liste
                }
                this.notifyDataSetChanged() // Notifie l'adapter que les données ont changé
            }
            .addOnFailureListener { exception ->
                // En cas d'échec de la récupération des données, log de l'erreur
                exception.message?.let { Log.e("error", it) }
            }
    }

    // ViewHolder pour chaque produit dans la liste RecyclerView
    class ProductViewHolder(private val binding: ProductViewBinding) : RecyclerView.ViewHolder(binding.root) {

        // Méthode pour lier les données d'un produit avec les vues
        fun bind(product: Product) {
            binding.apply {
                tvRef.text = "Référence: ${product.ref}" // Affichage de la référence du produit
                tvPrice.text = "Prix: ${product.price} DTN" // Affichage du prix du produit
                tvQuantity.text = "Quantité: ${product.quantity}" // Affichage de la quantité du produit
                if (product.imageUrl.equals("")) {
                    ivPicture.setImageDrawable(Drawable.createFromPath("res/drawable/picture.xml")) // Utilisation d'une image par défaut si aucune image n'est définie
                } else {
                    Picasso.get().load(product.imageUrl).fit().into(ivPicture) // Chargement de l'image à partir de l'URL avec Picasso
                }
            }
        }
    }
}
