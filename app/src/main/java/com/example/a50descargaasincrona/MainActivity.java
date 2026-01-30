package com.example.a50descargaasincrona;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private EditText etUrl;
    private ImageView imageView;
    // Hilo para la descarga
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    // Para obtener el contenido info
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etUrl = findViewById(R.id.etUrl);
        imageView = findViewById(R.id.imageView);
        Button btnDownload = findViewById(R.id.button);

        btnDownload.setOnClickListener(v -> {
            String url = etUrl.getText().toString();
            if (!url.isEmpty()) {
                downloadImage(url);
            }
        });
    }

    private void downloadImage(String urlString) {
        executor.execute(() -> {
            try {
                //1.- Descarga la imagen
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);

                //2.- Guardar en SD
                // Usamos getExternalFilesDir oara no pelearnos con permisos complejos
                File storageDir = getExternalFilesDir(null);
                File imageFile = new File(storageDir, "downloadedImage.png");
                FileOutputStream out = new FileOutputStream(imageFile);
                myBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                out.close();

                //3.- Actualizar la interfaz
                handler.post(() -> {
                    imageView.setImageBitmap(BitmapFactory.decodeFile(imageFile.getAbsolutePath()));
                    Toast.makeText(MainActivity.this, "Imagen descargada y guardada en: " +
                            imageFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
                });
            } catch (Exception e) {
                e.printStackTrace();
                handler.post(() -> Toast.makeText(MainActivity.this,
                        "Error al descargar la imagen", Toast.LENGTH_LONG).show());
            }
        });
    }
}