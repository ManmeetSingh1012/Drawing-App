package com.example.drawingapp

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.Image
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Message
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream


class MainActivity : AppCompatActivity() {

    private var drawingView:DrawingView?=null
    private var ButtonForSize:ImageButton?=null
    private var slectedpaint:ImageButton?=null
    private var PhotoSelection:ImageButton?=null
    private var undo:ImageButton?=null
    private var photosave:ImageButton?=null
    private var dialogProgress:Dialog?=null

    private var pickPhotoIntent: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
         result ->
            if(result.resultCode == RESULT_OK && result.data!=null)
            {
                val imagebackground:ImageView=findViewById(R.id.back_image)
                imagebackground.setImageURI(result.data?.data) // reuslt data is url of the photo and last data depict that photo
            }
        }

    private var permission:ActivityResultLauncher<Array<String>> =registerForActivityResult(
ActivityResultContracts.RequestMultiplePermissions()){
            permissions ->
        permissions.entries.forEach{
            val key=it.key
            val value=it.value

            if(value)
            {
                Toast.makeText(this,"Permission Given",Toast.LENGTH_SHORT).show()
                // Intent use to go from one acitvity to antother and from one app to another app as well
                // will pick photo from other app
                var pickIntent= Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                pickPhotoIntent.launch(pickIntent)
            }

            else {
                if (key == Manifest.permission.READ_EXTERNAL_STORAGE)
                {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
            }

        }





    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        drawingView=findViewById(R.id.drawing_view) // here drawing view our own view created
        drawingView!!.setBrushSize(10.toFloat())

        ButtonForSize=findViewById(R.id.brush)
        ButtonForSize?.setOnClickListener {
            showbrushDialog()
        }




        val paintlist=findViewById<LinearLayout>(R.id.color_plate)
        slectedpaint=paintlist[4] as ImageButton
       slectedpaint!!.setImageDrawable(

            ContextCompat.getDrawable( this ,R.drawable.color_chossed)
       )

        PhotoSelection=findViewById(R.id.photo)
        PhotoSelection?.setOnClickListener {
            photoselector()
        }

        undo=findViewById(R.id.undo)
        undo?.setOnClickListener {
            drawingView!!.undomethod()
        }

         photosave=findViewById(R.id.Save)
        photosave?.setOnClickListener {
            if(isStorageallowed())
            {
                progressbar()
                lifecycleScope.launch(Dispatchers.IO)
                {

                    val drawingView:FrameLayout=findViewById(R.id.contaner_for_diff_layer)
                    // we have created the function get bitmap take bitmap from view than we have passed to save bitmap file wich will save the file
                    saveBitmapFile(getbitmap(drawingView))
                }
            }
        }


    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun photoselector() {
        //shouldShowRequestPermissionRationale will return true for when permission is denied


        // true when permission is denied
        // false permision is not given or accepted

            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {
                dialog(
                    "Cant acess the external storage becuase permission is denied",
                    "Permission Error"
                )
            }


        else
        {
            permission.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE))

        }




    }
    // checking if the storage allowed so that we can save our file
    private fun isStorageallowed():Boolean
    {
        var permission=ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)

        return permission == PackageManager.PERMISSION_GRANTED


    }

    private fun dialog(message:String,title:String) {
        var dialog=AlertDialog.Builder(this)
        dialog.setIcon(android.R.drawable.ic_dialog_alert)
        dialog.setTitle(title)
        dialog.setMessage(message)

        dialog.setPositiveButton("Cancel"){cancel,_->
            Toast.makeText(this,"Permission Denied ",Toast.LENGTH_SHORT).show()}

        dialog.setCancelable(false)
        dialog.show()

    }

    // here dialog stands for the dialog box
    fun showbrushDialog()
    {
        var dialog=Dialog(this)
        // why we are using layout because the file is xml and not having id and we have to use the whole file
        dialog.setContentView(R.layout.buttons_brushsize)
        dialog.setTitle("Btn:")

        var btnVerysmall:ImageButton=dialog.findViewById(R.id.very_small)
        btnVerysmall.setOnClickListener {
            drawingView!!.setBrushSize(5.toFloat())
            dialog.dismiss()
        }

        var btnsmall:ImageButton=dialog.findViewById(R.id.small_brush)
        btnsmall.setOnClickListener {
            drawingView!!.setBrushSize(10.toFloat())
            dialog.dismiss()
        }

        var btnMedium:ImageButton=dialog.findViewById(R.id.medium_brush)
        btnMedium.setOnClickListener {
            drawingView!!.setBrushSize(20.toFloat())
            dialog.dismiss()
        }

        var btnlarge:ImageButton=dialog.findViewById(R.id.large_brush)
        btnlarge.setOnClickListener {
            drawingView!!.setBrushSize(30.toFloat())
            dialog.dismiss()
        }

        dialog.show()
    }

    fun color_chosed(view:View)
    {
        if(view!== slectedpaint)
        {
            var button=view as ImageButton
            var tag=button.tag.toString()
            drawingView?.color(tag)

            button.setImageDrawable(
                ContextCompat.getDrawable(this,R.drawable.color_chossed)
            )

            slectedpaint?.setImageDrawable(
                ContextCompat.getDrawable(this,R.drawable.color_sty)
            )

            slectedpaint=view


        }
    }

    // we use to get bitmap  or photo from view
    // here we have created bitmap to hold the pixel and canvas to draw on it
    // then we have set the background if we have
    // after we have draw or render all the things in the view on the canvas
    // But we know that bitmap holds the pixel so we returned the bitmap as a photo
    private fun getbitmap(view: View):Bitmap{
        val returnedBitmap=Bitmap.createBitmap(view.width,view.height,Bitmap.Config.ARGB_8888)

        val canvas = Canvas(returnedBitmap)
        val bgdrawable=view.background
        if (bgdrawable!=null)
        {
            bgdrawable.draw(canvas)

        }else
        {
            canvas.drawColor(Color.WHITE)

        }
        view.draw(canvas) // copying all the data present in the view on the newely created bitmap so we can import bitmap or photo

        return returnedBitmap
    }
// this will take bitmap file and convert into the file to store it
    private suspend fun saveBitmapFile(bitmap: Bitmap?):String
    {
        var result=""
        withContext(Dispatchers.IO)
        {
            if(bitmap!=null)
            {
                try {
                    val bytes=ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.PNG,90,bytes)

                    val fl=File(externalCacheDir?.absoluteFile.toString()+File.separator+
                            "Kids_Drawing"+System.currentTimeMillis()/1000+".png")

                    val fo=FileOutputStream(fl) // by this we can write on to the file
                    fo.write(bytes.toByteArray()) // we are writting our phot
                    fo.close()

                    result=fl.absolutePath // it will be the path where we will store our file


                    runOnUiThread{
                        if (result.isNotEmpty())
                        {
                            dismissPrgress()
                            Toast.makeText(this@MainActivity , "Filed saved Sucessfully :$result",Toast.LENGTH_SHORT).show()
                        }
                        else
                        {
                            Toast.makeText(this@MainActivity , "Something went wrong while saveing the file",Toast.LENGTH_SHORT).show()
                        }

                    }
                }
                catch (e:Exception)
                {
                    result=""
                    e.printStackTrace()
                }
            }

        }
        return result
    }

    private fun progressbar()
    {
        dialogProgress=Dialog(this@MainActivity)
        dialogProgress?.setContentView(R.layout.progressbar)

        dialogProgress?.show()
    }

    private fun dismissPrgress()
    {
        if(dialogProgress!=null)
        {
            dialogProgress?.dismiss()
            dialogProgress=null
        }
    }




}