package mini_project.group_ten;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.Landmark;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.FileNotFoundException;
import java.io.InputStream;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import static com.theartofdev.edmodo.cropper.CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_image);
        initViews();

    }

    //http://thezepr.com/index.html

    private void initViews() {
        detector = new FaceDetector.Builder(getApplicationContext())
                .setTrackingEnabled(false)
                .setLandmarkType(FaceDetector.ALL_CLASSIFICATIONS)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();

        imgTakePicture  = findViewById(R.id.imgTakePic);
        txtTakenPicDesc = findViewById(R.id.txtTakenPicDesc);

        imgTakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},CROP_IMAGE_ACTIVITY_REQUEST_CODE);
                }else{
                    txtTakenPicDesc.setText(null);
                    selectImage();
                }
            }
        });

    }

    private void selectImage(){
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                selectImage();
            }else{
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri imageUri = result.getUri();
                Bitmap bitmap;
                try {
                    InputStream inputStream = this.getContentResolver().openInputStream(imageUri);
                    bitmap = BitmapFactory.decodeStream(inputStream);
                    processInputImage(bitmap);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, "error while loading photo", Toast.LENGTH_SHORT).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void processInputImage(Bitmap bitmap) {
        imgTakePicture.setImageBitmap(bitmap);
        if (detector.isOperational() && bitmap != null) {

            editedBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                    .getHeight(), bitmap.getConfig());
            float scale = getResources().getDisplayMetrics().density;
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(Color.GREEN);
            paint.setTextSize((int) (12 * scale));
            paint.setShadowLayer(1f, 0f, 1f, Color.WHITE);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(6f);
            Canvas canvas = new Canvas(editedBitmap);
            canvas.drawBitmap(bitmap, 0, 0, paint);
            Frame frame = new Frame.Builder().setBitmap(editedBitmap).build();
            SparseArray<Face> faces = detector.detect(frame);

            for (int index = 0; index < faces.size(); ++index) {
                Face face = faces.valueAt(index);
                canvas.drawRect(
                        face.getPosition().x,
                        face.getPosition().y,
                        face.getPosition().x + face.getWidth(),
                        face.getPosition().y + face.getHeight(), paint);


                canvas.drawText("Face " + (index + 1), face.getPosition().x + face.getWidth(), face.getPosition().y + face.getHeight(), paint);

                txtTakenPicDesc.setText(txtTakenPicDesc.getText() + "\n" + "FACE " + (index + 1) + "\n");
                txtTakenPicDesc.setText(txtTakenPicDesc.getText() + "Smile probability:" + " " + face.getIsSmilingProbability() + "\n");
                txtTakenPicDesc.setText(txtTakenPicDesc.getText() + "Left Eye Is Open Probability: " + " " + face.getIsLeftEyeOpenProbability() + "\n");
                txtTakenPicDesc.setText(txtTakenPicDesc.getText() + "Right Eye Is Open Probability: " + " " + face.getIsRightEyeOpenProbability() + "\n\n");

                for (Landmark landmark : face.getLandmarks()) {
                    int cx = (int) (landmark.getPosition().x);
                    int cy = (int) (landmark.getPosition().y);
                    canvas.drawCircle(cx, cy, 8, paint);
                }
            }

            if (faces.size() == 0) {
                txtTakenPicDesc.setText("Scan Failed: Found nothing to scan");
            } else {
                imgTakePicture.setImageBitmap(editedBitmap);
            }
        }
        else {
            txtTakenPicDesc.setText("Could not set up the detector!");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        detector.release();
    }

    //---------------------------------------------------------------------------------
    ImageView imgTakePicture;
    TextView txtTakenPicDesc;
    private FaceDetector detector;
    Bitmap editedBitmap;

}


