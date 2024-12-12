package com.example.gestprod.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ContentValues.TAG
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
import com.example.gestprod.databinding.ActivityUpdateProductBinding
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.squareup.picasso.Picasso
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

class UpdateProductActivity : AppCompatActivity() {
    private val CAMERA_REQUEST_CODE = 1

    private lateinit var permissionAlertText: String
    private lateinit var galleryPermissionDeniedToastText: String

    private lateinit var controller: Controller
    private var imageUri: Uri? = null
    private lateinit var imageUrl: String
    lateinit var currentPhotoPath: String

    private lateinit var binding: ActivityUpdateProductBinding

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Récupération des chaînes de texte depuis les ressources
        permissionAlertText = resources.getText(R.string.alert_msg).toString()
        galleryPermissionDeniedToastText = resources.getText(R.string.gallery_toast_msg).toString()
        controller = Controller.getInstance()

        // Gestionnaire de clic pour retourner à l'écran d'accueil
        binding.btnGoHome.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Gestionnaire de clic pour rechercher un produit par référence
        binding.ivLoop.setOnClickListener {
            val reference = binding.etReference.text.toString()
            controller.searchProduct(reference) { product ->
                Log.d(TAG, "Product returned : ${product.toString()}")
                if (product != null) {
                    // Affichage des détails du produit trouvé
                    binding.etReference.isEnabled = false
                    binding.cardPrice.visibility = View.VISIBLE
                    binding.cardQuantity.visibility = View.VISIBLE
                    binding.frameLayout.visibility = View.VISIBLE
                    binding.ivCamera.visibility = View.VISIBLE
                    binding.ivGallery.visibility = View.VISIBLE
                    binding.btnUpdate.visibility = View.VISIBLE

                    binding.apply {
                        binding.etPrice.setText("${product.price}")
                        binding.etQuantity.setText("${product.quantity}")
                        if (!product.imageUrl.equals("")) {
                            binding.ivFrame.visibility = View.INVISIBLE
                            binding.ivProduct.visibility = View.VISIBLE
                            Picasso.get().load(product.imageUrl).into(binding.ivProduct)
                            imageUrl = product.imageUrl.toString()
                        }
                    }
                } else {
                    // Aucun produit trouvé avec cette référence
                    binding.cardPrice.visibility = View.INVISIBLE
                    binding.cardQuantity.visibility = View.INVISIBLE
                    binding.frameLayout.visibility = View.INVISIBLE
                    binding.ivCamera.visibility = View.INVISIBLE
                    binding.ivGallery.visibility = View.INVISIBLE

                    Toast.makeText(applicationContext, "There is no product with this reference!", Toast.LENGTH_SHORT).show()
                    Log.e(TAG, "Product does not exist for reference: $reference")
                }
            }
        }

        // Gestionnaires de clic pour ajuster le prix et la quantité
        binding.ivMinusPrice.setOnClickListener {
            val price = binding.etPrice.text.toString().toDouble()
            if ((price - 1) > 0.0)
                binding.etPrice.setText((price - 1).toString())
        }

        binding.ivPlusPrice.setOnClickListener {
            val price = binding.etPrice.text.toString().toDouble()
            binding.etPrice.setText((price + 1).toString())
        }

        binding.ivMinusQuantity.setOnClickListener {
            val quantity = binding.etQuantity.text.toString().toInt()
            if ((quantity - 1) > 0)
                binding.etQuantity.setText((quantity - 1).toString())
        }

        binding.ivPlusQuantity.setOnClickListener {
            val quantity = binding.etQuantity.text.toString().toInt()
            binding.etQuantity.setText((quantity + 1).toString())
        }

        // Gestionnaire de clic pour ouvrir l'appareil photo
        binding.ivCamera.setOnClickListener {
            checkPermissionForCamera()
        }

        // Gestionnaire de clic pour ouvrir la galerie
        binding.ivGallery.setOnClickListener {
            checkPermissionForGallery()
        }

        // Gestionnaire de clic pour ouvrir la boîte de dialogue de sélection de photo
        binding.ivFrame.setOnClickListener {
            openPhotoDialog()
        }

        // Gestionnaire de clic pour afficher l'image en grand et ouvrir la boîte de dialogue de sélection de photo
        binding.ivProduct.setOnClickListener {
            openPhotoDialog()
        }

        // Gestionnaire de clic pour mettre à jour le produit
        binding.btnUpdate.setOnClickListener {
            val reference = binding.etReference.text.toString()
            val priceStr = binding.etPrice.text.toString()
            val quantityStr = binding.etQuantity.text.toString()

            val price = if (priceStr.isNotEmpty()) priceStr.toDouble() else 0.0
            val quantity = if (quantityStr.isNotEmpty()) quantityStr.toInt() else 0

            imageUri?.let { controller.updateProduct(reference, price, quantity, it, this) }
            Log.i("MyInfo", "In btnUpdate listener, product updated : $reference")
        }
    }

    // Gestionnaire de résultat pour récupérer une image de la galerie
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            imageUri = data?.data
            imageUri?.let {
                binding.ivFrame.visibility = View.INVISIBLE
                binding.ivProduct.visibility = View.VISIBLE
                binding.ivProduct.load(imageUri) {
                    crossfade(true)
                    crossfade(1000)
                    transformations(RoundedCornersTransformation(8.0F))
                }
                Toast.makeText(this, "Selected Image: $it", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Ouvre la boîte de dialogue pour choisir entre la galerie et l'appareil photo
    private fun openPhotoDialog() {
        val photoDialog = AlertDialog.Builder(this)
        photoDialog.setTitle("Select Action")
        photoDialog.setItems(arrayOf(
            resources.getText(R.string.photo_dialogue_item1).toString(),
            resources.getText(R.string.photo_dialogue_item2).toString()
        )) { _, which ->
            when (which) {
                0 -> pickImageFromGallery()
                1 -> dispatchTakePictureIntent()
            }
        }
        photoDialog.show()
    }

    // Lance l'intention de sélectionner une image depuis la galerie
    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        pickImageLauncher.launch(intent)
    }

    // Vérifie les permissions pour accéder à la galerie
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun checkPermissionForGallery() {
        val permissions = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        Dexter.withContext(this)
            .withPermissions(permissions)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report != null && report.areAllPermissionsGranted()) {
                        // Toutes les permissions sont accordées, procéder à la sélection d'image
                        pickImageFromGallery()
                    } else {
                        // Permission refusée, afficher un toast ou informer l'utilisateur
                        Toast.makeText(this@UpdateProductActivity, "Permission denied", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    // Affiche une explication si nécessaire, par exemple, explique pourquoi les permissions sont nécessaires
                    token?.continuePermissionRequest()
                }
            })
            .check()
    }

    // Vérifie les permissions pour accéder à l'appareil photo
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun checkPermissionForCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)
        } else {
            dispatchTakePictureIntent()
        }
    }

    // Traite le résultat de l'intention de l'appareil photo
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == CAMERA_REQUEST_CODE) {

            binding.ivFrame.visibility = View.INVISIBLE
            binding.ivProduct.visibility = View.VISIBLE

            var file = File(currentPhotoPath)

            binding.ivProduct.load(file) {
                crossfade(true)
                crossfade(1000)
                transformations(RoundedCornersTransformation(8.0F))
            }
            Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { mediaScanIntent ->
                val file = File(currentPhotoPath)
                imageUri = Uri.fromFile(file)
                mediaScanIntent.data = imageUri
                sendBroadcast(mediaScanIntent)
            }
        }
    }

    // Crée un fichier d'image temporaire
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Crée un nom de fichier d'image avec horodatage
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        //val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* préfixe */
            ".jpg", /* suffixe */
            storageDir /* répertoire */
        ).apply {
            // Enregistre le chemin du fichier pour une utilisation avec les intentions ACTION_VIEW
            currentPhotoPath = absolutePath
            Log.i("my Info", "Here in createImageFile() -- $currentPhotoPath")
        }
    }

    // Lance l'intention de prendre une photo
    @SuppressLint("QueryPermissionsNeeded")
    private fun dispatchTakePictureIntent() {
        Log.i("my Info", "Here in AddProductActivity,dispatchTakePictureIntent()")
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Vérifie s'il existe une activité de caméra pour gérer l'intention
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Crée le fichier où la photo doit être enregistrée
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Une erreur s'est produite lors de la création du fichier
                    ex.printStackTrace() // Gère l'erreur de manière appropriée
                    null
                }
                // Continue seulement si le fichier a été créé avec succès
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.example.gestprod.fileprovider", // Remplacez par votre autorité réelle
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    // Vérifie si l'appareil prend en charge cette intention avant de démarrer l'activité
                    if (takePictureIntent.resolveActivity(packageManager) != null) {
                        startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE)
                    } else {
                        Toast.makeText(this, "No camera app found", Toast.LENGTH_SHORT).show()
                    }
                }
            } ?: run {
                Toast.makeText(this, "No camera app found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Gère la réponse de la demande de permissions
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent()
            } else {
                showRotationalDialogForPermission()
            }
        }
    }

    // Affiche une boîte de dialogue pour rediriger vers les paramètres d'application pour gérer les permissions
    private fun showRotationalDialogForPermission() {
        AlertDialog.Builder(this)
            .setMessage(permissionAlertText)
            .setPositiveButton("SETTINGS") { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)

                } catch (exception: ActivityNotFoundException) {
                    exception.printStackTrace()
                }
            }
            .setNegativeButton("CANCEL") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }
}
