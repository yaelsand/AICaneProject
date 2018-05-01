package org.tensorflow.demo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {

    static int SELECT_FILE;
    ImageView ivImage;
    private Classifier detector;
    private TextToSpeech tts;
    private TextView recognize;

    // Which detection model to use: by default uses Tensorflow Object Detection API frozen
    // checkpoints.  Optionally use legacy Multibox (trained using an older version of the API)
    // or YOLO.
    private enum DetectorMode {
        TF_OD_API, MULTIBOX, YOLO;
    }
    private static final MainActivity.DetectorMode MODE = MainActivity.DetectorMode.TF_OD_API;

    // Minimum detection confidence to track a detection.
    private static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.2f; //0.6f
    private static final float MINIMUM_CONFIDENCE_MULTIBOX = 0.1f;
    private static final float MINIMUM_CONFIDENCE_YOLO = 0.25f;

//    private static final int TF_OD_API_INPUT_SIZE = 300;
    private static final String TF_OD_API_MODEL_FILE = "file:///android_asset/ssd_mobilenet_v1_android_export.pb";
    private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/coco_labels_list.txt";


    /* Configuration values for tiny-yolo-voc. Note that the graph is not included with TensorFlow and
    must be manually placed in the assets/ directory by the user.
    Graphs and models downloaded from http://pjreddie.com/darknet/yolo/ may be converted e.g. via
    DarkFlow (https://github.com/thtrieu/darkflow). Sample command:
    ./flow --model cfg/tiny-yolo-voc.cfg --load bin/tiny-yolo-voc.weights --savepb --verbalise */
    private static final String YOLO_MODEL_FILE = "file:///android_asset/graph-tiny-yolo-voc.pb";
//    private static final int YOLO_INPUT_SIZE = 416;
    private static final String YOLO_INPUT_NAME = "input";
    private static final String YOLO_OUTPUT_NAMES = "output";
    private static final int YOLO_BLOCK_SIZE = 32;

    // Configuration values for the prepackaged multibox model.
//    private static final int MB_INPUT_SIZE = 224;
    private static final int MB_IMAGE_MEAN = 128;
    private static final float MB_IMAGE_STD = 128;
    private static final String MB_INPUT_NAME = "ResizeBilinear";
    private static final String MB_OUTPUT_LOCATIONS_NAME = "output_locations/Reshape";
    private static final String MB_OUTPUT_SCORES_NAME = "output_scores/Reshape";
    private static final String MB_MODEL_FILE = "file:///android_asset/multibox_model.pb";
    private static final String MB_LOCATION_FILE = "file:///android_asset/multibox_location_priors.txt";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button choosePic = (Button) findViewById(R.id.choosePictureBtn);
        recognize = (TextView) findViewById(R.id.recognize);
        ivImage = (ImageView) findViewById(R.id.ivImage);
        choosePic.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*"); //TODO need video: intent.setType("image/* video/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);//
                startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);
            }
        });


        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS)
                {
                    tts.setLanguage(Locale.US);
                }
                else
                    System.out.println("TTS ERROR");
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap bitmap = null;
        if (data != null) {
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int selected_size = height > width ? height : width;

        ivImage.setImageBitmap(bitmap);
        switch (MODE) {
            case YOLO:
                detector = TensorFlowYoloDetector.create(
                        getAssets(), YOLO_MODEL_FILE, selected_size,
                        YOLO_INPUT_NAME, YOLO_OUTPUT_NAMES, YOLO_BLOCK_SIZE);
                break;
            case MULTIBOX:
                detector = TensorFlowMultiBoxDetector.create(
                        getAssets(), MB_MODEL_FILE, MB_LOCATION_FILE, MB_IMAGE_MEAN,
                        MB_IMAGE_STD, MB_INPUT_NAME, MB_OUTPUT_LOCATIONS_NAME, MB_OUTPUT_SCORES_NAME);
                break;
            default: // TF_OD_API
                try {
                    detector = TensorFlowObjectDetectionAPIModel.create(
                            getAssets(), TF_OD_API_MODEL_FILE, TF_OD_API_LABELS_FILE, selected_size);
                } catch (final IOException e) {
                    Toast toast = Toast.makeText(
                            getApplicationContext(), "Classifier could not be initialized", Toast.LENGTH_SHORT);
                    toast.show();
                    finish();
                }
                break;
        }

        final List<Classifier.Recognition> results = detector.recognizeImage(bitmap);
        final List<Classifier.Recognition> confidenceResults = new ArrayList<>();

        float minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
        switch (MODE) {
            case TF_OD_API:
                minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
                break;
            case MULTIBOX:
                minimumConfidence = MINIMUM_CONFIDENCE_MULTIBOX;
                break;
            case YOLO:
                minimumConfidence = MINIMUM_CONFIDENCE_YOLO;
                break;
        }

        //
//        Bitmap copyBitmap = Bitmap.createBitmap(bitmap);
//        final Canvas canvas = new Canvas(copyBitmap);
//        final Paint paint = new Paint();
//        paint.setColor(Color.RED);
//        paint.setStyle(Paint.Style.STROKE);
//        paint.setStrokeWidth(2.0f);


        recognize.setText("");
        for (final Classifier.Recognition result : results) {
            if (result.getLocation() != null && result.getConfidence() >= minimumConfidence) {
                System.out.println("-------------- Result: " + result.toString());
                confidenceResults.add(result);
//                canvas.drawRect(result.getLocation(), paint);
                recognize.setText(recognize.getText() + "\n" + result.toString());
                tts.speak(result.getTitle(), TextToSpeech.QUEUE_FLUSH, null, null);
            }
//            else
//                System.out.println("--------------- No confidence: " + result.toString());
        }
    }
}
