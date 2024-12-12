package com.example.gestprod.model

import android.content.ContentValues.TAG
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class FirestoreManager {
    private val db = Firebase.firestore
    private lateinit var storageRef : StorageReference
    private lateinit var firebaseFirestore: FirebaseFirestore

    // Méthode pour ajouter un produit avec une image sur Firestore
    fun addProduct(reference: String, price: Double, quantity: Int, imageUri: Uri?, context: Context?) {
        init() // Initialisation des références Firebase

        // Création de la référence de stockage pour l'image du produit
        storageRef = storageRef.child("img:$reference:${System.currentTimeMillis()}")

        // Vérification et ajout de l'image si elle est fournie
        imageUri?.let {
            storageRef.putFile(it).addOnCompleteListener{ task ->
                if(task.isSuccessful){
                    // Récupération de l'URL de l'image téléchargée
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        // Création d'un objet HashMap pour représenter le produit
                        val product = hashMapOf(
                            "reference" to reference,
                            "price" to price,
                            "quantity" to quantity,
                            "picture" to uri.toString()
                        )
                        // Ajout du produit à la collection "Products" sur Firestore
                        db.collection("Products")
                            .add(product)
                            .addOnSuccessListener { documentReference ->
                                Log.d(TAG, "DocumentSnapshot ajouté avec l'ID: ${documentReference.id}")
                                Toast.makeText(context, "Product uploaded successfully", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Log.w(TAG, "Error adding document", e)
                            }
                    }
                }else{
                    // Affichage de l'erreur si le téléchargement de l'image échoue
                    Toast.makeText(context, task.exception?.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Méthode pour rechercher un produit sur Firestore par référence
    fun searchProduct(reference: String, callback: (Product?) -> Unit) {
        firebaseFirestore = FirebaseFirestore.getInstance()
        val collection = firebaseFirestore.collection("Products")
        val searchQuery = collection.whereEqualTo("reference", reference)

        searchQuery.get()
            .addOnSuccessListener { querySnapshot ->
                var product: Product? = null
                for (document in querySnapshot.documents) {
                    if (document.exists()) {
                        // Récupération des données du produit trouvé
                        val reference = document.getString("reference") ?: ""
                        val price = document.getDouble("price") ?: 0.0
                        val quantity = document.getLong("quantity") ?: 0
                        val pictureUrl = document.getString("picture") ?: ""

                        // Création de l'objet Product correspondant
                        product = Product(reference, price, quantity, pictureUrl)
                        Log.d(TAG, "Product found: $product")
                    } else {
                        // Affichage d'une erreur si le document n'existe pas
                        Log.e(TAG, "Document does not exist for reference: $reference")
                    }
                }
                callback(product) // Pass the product back via callback
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error fetching document: $e")
                callback(null) // Notify failure via callback
            }
    }

    // Méthode pour supprimer un produit et son image de Firestore et Firebase Storage
    fun deleteProduct(reference: String, context: Context) {
        firebaseFirestore = FirebaseFirestore.getInstance()
        val collection = firebaseFirestore.collection("Products")
        val searchQuery = collection.whereEqualTo("reference", reference)

        searchQuery.get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot.documents) {
                    if (document.exists()) {
                        // Récupération de l'URL de l'image à supprimer depuis Firestore
                        val imageUrl = document.getString("picture")
                        if (imageUrl != null) {
                            val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)

                            // Suppression du document de Firestore
                            document.reference.delete()
                                .addOnSuccessListener {
                                    Log.d(TAG, "DocumentSnapshot successfully deleted!")

                                    // Suppression de l'image depuis Firebase Storage
                                    storageRef.delete()
                                        .addOnSuccessListener {
                                            Log.d(TAG, "Product image deleted successfully!")
                                            Toast.makeText(context, "Product deleted successfully!", Toast.LENGTH_SHORT).show()
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e(TAG, "Error deleting product image: $e")
                                            Toast.makeText(context, "Failed to delete product image!", Toast.LENGTH_SHORT).show()
                                        }
                                }
                                .addOnFailureListener { e ->
                                    Log.e(TAG, "Error deleting document: $e")
                                    Toast.makeText(context, "Failed to delete product!", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            // Affichage d'une erreur si aucune URL d'image n'est trouvée
                            Log.e(TAG, "No imageUrl found in document: $reference")
                            Toast.makeText(context, "Failed to delete product image: No image found!", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // Affichage d'une erreur si aucun document correspondant n'est trouvé
                        Log.e(TAG, "No such document for reference: $reference")
                        Toast.makeText(context, "Failed to delete product: Document not found!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .addOnFailureListener { e ->
                // Affichage d'une erreur en cas d'échec de la récupération du document
                Log.e(TAG, "Error fetching document: $e")
                Toast.makeText(context, "Failed to delete product: Error fetching document!", Toast.LENGTH_SHORT).show()
            }
    }

    // Méthode pour mettre à jour un produit sur Firestore, avec éventuellement une nouvelle image
    fun updateProduct(reference: String, price: Double, quantity: Int, imageUri: Uri?, context: Context?) {
        firebaseFirestore = FirebaseFirestore.getInstance()
        val collection = firebaseFirestore.collection("Products")
        val searchQuery = collection.whereEqualTo("reference", reference)

        searchQuery.get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot.documents) {

                    var updates = hashMapOf<String, Any>(
                        "price" to price,
                        "quantity" to quantity
                    )

                    // Vérification s'il y a une nouvelle image à mettre à jour
                    if (imageUri != null) {
                        // Suppression de l'image existante depuis le stockage s'il y en a une
                        val currentImageUrl = document.getString("picture")
                        if (!currentImageUrl.isNullOrEmpty()) {
                            val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(currentImageUrl)
                            storageReference.delete()
                                .addOnSuccessListener {
                                    Log.d(TAG, "Existing image deleted successfully")
                                }
                                .addOnFailureListener { e ->
                                    Log.e(TAG, "Error deleting existing image: $e")
                                }
                        }

                        // Téléchargement de la nouvelle image vers le stockage
                        val storageRef = FirebaseStorage.getInstance().reference
                            .child("Images/img:$reference:${System.currentTimeMillis()}")

                        storageRef.putFile(imageUri)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                                        updates["picture"] = uri.toString()
                                        // Mise à jour du document avec la nouvelle URL d'image
                                        document.reference.update(updates)
                                            .addOnSuccessListener {
                                                Log.d(TAG, "DocumentSnapshot successfully updated with new image!")
                                                Toast.makeText(context, "Product updated successfully!", Toast.LENGTH_SHORT).show()
                                            }
                                            .addOnFailureListener { e ->
                                                Log.e(TAG, "Error updating document with new image: $e")
                                                Toast.makeText(context, "Failed to update product!", Toast.LENGTH_SHORT).show()
                                            }
                                    }
                                } else {
                                    // Affichage d'un message d'erreur si le téléchargement de la nouvelle image échoue
                                    Toast.makeText(context, "Failed to upload new image: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                    }
                }
            }
            .addOnFailureListener { e ->
                // Affichage d'une erreur en cas d'échec de la récupération du document
                Log.e(TAG, "Error fetching document: $e")
                Toast.makeText(context, "Failed to update product: Error fetching document!", Toast.LENGTH_SHORT).show()
            }
    }

    // Méthode privée pour initialiser les références Firebase
    private fun init() {
        storageRef = FirebaseStorage.getInstance().reference.child("Images")
        firebaseFirestore = FirebaseFirestore.getInstance()
    }

}
