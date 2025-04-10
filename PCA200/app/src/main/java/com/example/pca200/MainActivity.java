package com.example.pca200;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import org.tensorflow.lite.Interpreter;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private static final int INPUT_SIZE = 224; // Model's expected input size
    private Interpreter interpreter;
    private ImageView imageView;
    private TextView resultText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        resultText = findViewById(R.id.resultText);
        Button selectButton = findViewById(R.id.selectButton);

        try {
            interpreter = new Interpreter(loadModelFile("model_pca200.tflite"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Handle image selection
        ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                            imageView.setImageBitmap(bitmap);
                            String prediction = runModel(bitmap);
                            resultText.setText(prediction);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

        selectButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickImageLauncher.launch(intent);
        });
    }

    // Load TFLite model from assets
    private MappedByteBuffer loadModelFile(String modelName) throws IOException {
        AssetFileDescriptor fileDescriptor = getAssets().openFd(modelName);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, fileDescriptor.getStartOffset(), fileDescriptor.getDeclaredLength());
    }

    // Preprocess image, run inference, and return the result
    private String runModel(Bitmap bitmap) {

        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true);
        float[][][][] inputData = processImage(resizedBitmap);
        float[][] outputData = new float[1][10];

        interpreter.run(inputData, outputData);

        int predictedIndex = 0;
        float maxConfidence = outputData[0][0];


        for (int i = 1; i < outputData[0].length; i++) {
            if (outputData[0][i] > maxConfidence) {
                maxConfidence = outputData[0][i];
                predictedIndex = i;
            }
        }

        return "Class " + predictedIndex + " (Confidence: " + maxConfidence + ")";
    }

    // Convert image to float array (normalize pixels: 0-1)
    private float[][][][] processImage(Bitmap bitmap) {
        float[][][][] imgData = new float[1][INPUT_SIZE][INPUT_SIZE][3];

        for (int y = 0; y < INPUT_SIZE; y++) {
            for (int x = 0; x < INPUT_SIZE; x++) {
                int pixel = bitmap.getPixel(x, y);
                imgData[0][y][x][0] = ((pixel >> 16) & 0xFF) / 255.0f; // Red
                imgData[0][y][x][1] = ((pixel >> 8) & 0xFF) / 255.0f;  // Green
                imgData[0][y][x][2] = (pixel & 0xFF) / 255.0f;         // Blue
            }
        }
        return imgData;
    }
}
