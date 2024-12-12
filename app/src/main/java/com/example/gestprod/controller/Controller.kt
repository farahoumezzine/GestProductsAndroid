package com.example.gestprod.controller

import android.content.Context
import android.net.Uri
import com.example.gestprod.model.FirestoreManager
import com.example.gestprod.model.Product

class Controller private constructor() {

    private val firestoreManager: FirestoreManager = FirestoreManager()

    // Méthode pour ajouter un produit via le FirestoreManager
    fun addProduct(reference: String, price: Double, quantity: Int, imageUri: Uri?, context: Context?) {
        firestoreManager.addProduct(reference, price, quantity, imageUri, context)
    }

    // Méthode pour rechercher un produit via le FirestoreManager
    fun searchProduct(reference: String, callback: (Product?) -> Unit) {
        firestoreManager.searchProduct(reference) { product ->
            callback(product)
        }
    }

    // Méthode pour supprimer un produit via le FirestoreManager
    fun deleteProduct(reference: String, context: Context) {
        firestoreManager.deleteProduct(reference, context)
    }

    // Méthode pour mettre à jour un produit via le FirestoreManager
    fun updateProduct(reference: String, price: Double, quantity: Int, selectedImageUri: Uri?, context: Context) {
        firestoreManager.updateProduct(reference, price, quantity, selectedImageUri, context)
    }

    companion object {
        private var instance: Controller? = null

        // Singleton pour obtenir l'instance unique de Controller
        fun getInstance(): Controller {
            if (instance == null) {
                instance = Controller()
            }
            return instance!!
        }
    }
}
