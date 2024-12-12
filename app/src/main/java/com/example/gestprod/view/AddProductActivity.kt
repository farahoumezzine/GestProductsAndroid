package com.example.gestprod.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.load
import coil.transform.RoundedCornersTransformation
import com.example.gestprod.R
import com.example.gestprod.controller.Controller
import com.example.gestprod.databinding.ActivityAddProductBinding
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date


class AddProductActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddProductBinding // Déclaration de la variable de liaison ActivityAddProductBinding
    private val CAMERA_REQUEST_CODE = 100 // Code de demande pour l'accès à la caméra

    private lateinit var permissionAlertText: String // Texte d'alerte pour les permissions
    private lateinit var galleryPermissionDeniedToastText: String // Message toast pour l'accès refusé à la galerie
    private lateinit var controller: Controller // Instance du contrôleur
    private var imageUri: Uri? = null // URI de l'image sélectionnée
    private lateinit var currentPhotoPath: String // Chemin de la photo actuelle

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddProductBinding.inflate(layoutInflater) // Inflation de la vue via la liaison ActivityAddProductBinding
        setContentView(binding.root) // Définition de la vue principale

        permissionAlertText = resources.getText(R.string.alert_msg).toString() // Récupération du texte d'alerte depuis les ressources
        galleryPermissionDeniedToastText = resources.getText(R.string.gallery_toast_msg).toString() // Récupération du message toast pour l'accès refusé à la galerie

        binding.btnGoHome.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java) // Création d'une intention vers HomeActivity
            startActivity(intent) // Démarrage de l'activité HomeActivity
            finish()
        }

        controller = Controller.getInstance() // Obtention de l'instance du contrôleur

        binding.btnAdd.setOnClickListener {

            val reference = binding.etReference.text.toString() // Récupération de la référence du produit depuis EditText
            val priceStr = binding.etPrice.text.toString() // Récupération du prix sous forme de chaîne depuis EditText
            val quantityStr = binding.etQuantity.text.toString() // Récupération de la quantité sous forme de chaîne depuis EditText

            // Validation et conversion du prix et de la quantité
            val price = if (priceStr.isNotEmpty()) priceStr.toDouble() else 0.0 // Conversion en double, si non vide
            val quantity = if (quantityStr.isNotEmpty()) quantityStr.toInt() else 0 // Conversion en entier, si non vide

            // Ajout du produit en utilisant le contrôleur
            controller.addProduct(reference, price, quantity, imageUri, this)
        }

        binding.ivCamera.setOnClickListener {
            checkPermissionForCamera() // Vérification des permissions pour la caméra
        }

        binding.ivGallery.setOnClickListener {
            checkPermissionForGallery() // Vérification des permissions pour la galerie
        }

        binding.ivFrame.setOnClickListener {
            openPhotoDialog() // Ouverture du dialogue de sélection de photo
        }

        binding.ivProduct.setOnClickListener {
            openPhotoDialog() // Ouverture du dialogue de sélection de photo
        }
    }

    // Gestion du résultat de la sélection d'image depuis la galerie
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            imageUri = data?.data // Récupération de l'URI de l'image sélectionnée depuis les données
            imageUri?.let {
                binding.ivFrame.visibility = View.INVISIBLE // Masquage du cadre de l'image
                binding.ivProduct.visibility = View.VISIBLE // Affichage de l'image sélectionnée
                binding.ivProduct.load(imageUri) {
                    crossfade(true) // Activation du fondu enchaîné
                    crossfade(1000) // Durée du fondu enchaîné en millisecondes
                    transformations(RoundedCornersTransformation(8.0F)) // Transformation des coins arrondis
                }
                Toast.makeText(this, "Selected Image: $it", Toast.LENGTH_LONG).show() // Affichage d'un toast avec l'URI de l'image sélectionnée
            }
        }
    }

    // Ouverture du dialogue de sélection de photo
    private fun openPhotoDialog() {
        val photoDialog = AlertDialog.Builder(this) // Création d'une boîte de dialogue avec le contexte de l'activité
        photoDialog.setTitle("Select Action") // Titre de la boîte de dialogue
        photoDialog.setItems(arrayOf(resources.getText(R.string.photo_dialogue_item1).toString(),
            resources.getText(R.string.photo_dialogue_item2).toString())){
                _, which ->
            when (which){
                0 -> pickImageFromGallery() // Sélection d'image depuis la galerie
                1 -> dispatchTakePictureIntent() // Capture de photo depuis la caméra
            }
        }
        photoDialog.show() // Affichage de la boîte de dialogue
    }

    // Sélection d'image depuis la galerie
    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT) // Intent pour ouvrir un document
        intent.addCategory(Intent.CATEGORY_OPENABLE) // Catégorie d'ouverture
        intent.type = "image/*" // Type MIME pour les images
        pickImageLauncher.launch(intent) // Lancement de l'intent pour choisir une image
    }

    // Vérification des permissions pour la galerie
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun checkPermissionForGallery() {
        val permissions = mutableListOf<String>() // Liste mutable pour les permissions

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES) // Ajout de la permission pour lire les images dans les médias
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE) // Ajout de la permission pour lire le stockage externe
        }

        Dexter.withContext(this) // Utilisation de Dexter avec le contexte de l'activité
            .withPermissions(permissions) // Ajout des permissions à Dexter
            .withListener(object : MultiplePermissionsListener { // Écouteur pour les permissions multiples
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report != null && report.areAllPermissionsGranted()) {
                        // Toutes les permissions sont accordées, procéder à la sélection de l'image
                        pickImageFromGallery()
                    } else {
                        // Permission refusée, affichage d'un toast ou information à l'utilisateur
                        Toast.makeText(this@AddProductActivity, "Permission denied", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    // Affichage du rationnel si nécessaire, par exemple expliquer pourquoi les permissions sont nécessaires
                    token?.continuePermissionRequest()
                }
            })
            .check() // Vérification des permissions avec Dexter
    }

    // Vérification des permissions pour la caméra
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun checkPermissionForCamera() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)
        } else {
            dispatchTakePictureIntent() // Autorisation déjà accordée, lancer l'intention de capture de photo
        }
    }

    // Gestion du résultat après avoir pris une photo
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == CAMERA_REQUEST_CODE) {

            binding.ivFrame.visibility = View.INVISIBLE // Masquage du cadre de l'image
            binding.ivProduct.visibility = View.VISIBLE // Affichage de l'image capturée

            val file = File(currentPhotoPath) // Récupération du fichier photo depuis le chemin actuel

            binding.ivProduct.load(file) { // Chargement de l'image dans ImageView avec Coil
                crossfade(true) // Activation du fondu enchaîné
                crossfade(1000) // Durée du fondu enchaîné en millisecondes
                transformations(RoundedCornersTransformation(8.0F)) // Transformation des coins arrondis
            }

            Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { mediaScanIntent ->
                val file = File(currentPhotoPath) // Récupération du fichier photo depuis le chemin actuel
                imageUri = Uri.fromFile(file) // Conversion en URI
                mediaScanIntent.data = imageUri // Définition de l'URI pour le balayage média
                sendBroadcast(mediaScanIntent) // Envoi de l'intention de balayage média
            }
        }
    }

    // Création d'un fichier image temporaire
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Création du nom de fichier avec horodatage
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) // Répertoire de stockage pour les images

        return File.createTempFile(
            "JPEG_${timeStamp}_", /* Préfixe du fichier */
            ".jpg", /* Suffixe du fichier */
            storageDir /* Répertoire */
        ).apply {
            currentPhotoPath = absolutePath // Enregistrement du chemin actuel du fichier
            Log.i("my Info", "Here in createImageFile() -- $currentPhotoPath")
        }
    }

    // Ouverture de l'intention pour capturer une photo
    @SuppressLint("QueryPermissionsNeeded")
    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile() // Création du fichier image
                } catch (ex: IOException) {
                    ex.printStackTrace() // Gestion de l'erreur en cas de problème avec la création du fichier
                    null
                }
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.example.gestprod.fileprovider", // Remplacez avec votre autorité réelle
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI) // Définition de l'URI pour sauvegarder l'image capturée
                    if (takePictureIntent.resolveActivity(packageManager) != null) {
                        startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE) // Lancement de l'intention de capture de photo
                    } else {
                        Toast.makeText(this, "No camera app found", Toast.LENGTH_SHORT).show() // Aucune application de caméra trouvée
                    }
                }
            } ?: run {
                Toast.makeText(this, "No camera app found", Toast.LENGTH_SHORT).show() // Aucune application de caméra trouvée
            }
        }
    }

    // Gestion de la réponse de demande de permissions
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent() // Permission accordée, lancer l'intention de capture de photo
            } else {
                showRotationalDialogForPermission() // Affichage de la boîte de dialogue pour demander les permissions
            }
        }
    }

    // Affichage de la boîte de dialogue pour demander les permissions
    private fun showRotationalDialogForPermission() {
        AlertDialog.Builder(this)
            .setMessage(permissionAlertText) // Message de la boîte de dialogue
            .setPositiveButton("SETTINGS") { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS) // Ouverture des paramètres de l'application
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent) // Démarrage de l'intention
                } catch (exception: ActivityNotFoundException) {
                    exception.printStackTrace() // Gestion des exceptions
                }
            }
            .setNegativeButton("CANCEL") { dialog, _ ->
                dialog.dismiss() // Annulation de la boîte de dialogue
            }.show() // Affichage de la boîte de dialogue
    }
}