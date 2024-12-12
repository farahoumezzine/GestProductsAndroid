package com.example.gestprod.model

// Définition de la classe Product comme une data class
data class Product(
    var ref: String = "",         // Propriété mutable de type String, initialisée par défaut à une chaîne vide
    var price: Double = 0.0,      // Propriété mutable de type Double, initialisée par défaut à 0.0
    var quantity: Long = 0,       // Propriété mutable de type Long, initialisée par défaut à 0
    var imageUrl: String? = null  // Propriété mutable de type String nullable, initialisée par défaut à null
) {
    // Redéfinition de la méthode toString() pour afficher les propriétés du produit
    override fun toString(): String {
        return "Product(ref='$ref', price=$price, quantity=$quantity, imageUrl=$imageUrl)"
    }
}
