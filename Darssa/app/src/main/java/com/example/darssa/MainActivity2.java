package com.example.darssa;

import static org.opencv.android.Utils.matToBitmap;
import androidx.appcompat.app.AppCompatActivity;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.content.Intent;
import android.graphics.Bitmap;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.core.Point;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.darssa.config.APIConfig;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity2 extends AppCompatActivity implements View.OnClickListener{

    private static final int CAMERA_REQUEST_CODE = 2;
    private boolean isProcessImageCalledvertical = false;
    private boolean isProcessImageCalledhorizontal = false;
    private String student,pw;
    Bitmap contoursBitmap;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int MAX_SELECTED_POINTS = 4;
    private ImageView imageView;
    private ImageView imageView1;

    private ImageView imageView2;

    private ImageView imageView3;

    private double somme;
    private int note;
    private int b=0;

    private ImageButton btnSelectImage,next,camera;
    private Button commencer;
    private List<Point> selectedPoints = new ArrayList<>();
    private Bitmap originalBitmap;

    TextView textViewResult1,textViewResult2,textViewResult3,textViewResult4,textViewResult5,textViewResult6;

    Button soumettre;


    {
        if (!OpenCVLoader.initDebug()) {
            // Handle initialization error
            Toast.makeText(MainActivity2.this, "OpenCV initialization failed.", Toast.LENGTH_SHORT).show();
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        btnSelectImage = findViewById(R.id.btnSelectImage);
        imageView = findViewById(R.id.imageView);
        imageView1 = findViewById(R.id.imageView1);
        imageView2 = findViewById(R.id.imageView2);
        imageView3 = findViewById(R.id.imageView3);
        next=findViewById(R.id.next);
        camera = findViewById(R.id.camera);
        commencer=findViewById(R.id.commencez);
        soumettre=findViewById(R.id.soumettre);
        textViewResult1 = findViewById(R.id.internes1);
        textViewResult2 = findViewById(R.id.internes2);
        textViewResult3 = findViewById(R.id.externes1);
        textViewResult4 = findViewById(R.id.externes2);
        textViewResult5 = findViewById(R.id.depouille1);
        textViewResult6 = findViewById(R.id.depouille2);


        Intent intent = getIntent();
        student = intent.getStringExtra("STUDENTid");
        pw = intent.getStringExtra("PWid");

        soumettre.setOnClickListener(this);




        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
                } else {
                    Toast.makeText(MainActivity2.this, "Impossible d'ouvrir l'appareil photo", Toast.LENGTH_SHORT).show();
                }
            }
        });



        commencer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (originalBitmap != null) {
                    selectedPoints.clear();
                    isProcessImageCalledvertical = false;
                    isProcessImageCalledhorizontal = false;
                    processImageHorizontal(originalBitmap,1);

                }else {
                    Toast.makeText(MainActivity2.this, "Pas de photo séléctionnée", Toast.LENGTH_SHORT).show();
                }
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (originalBitmap != null) {

                    processImageHorizontal(originalBitmap,0);


                    // Réinitialiser la liste des points sélectionnés
                    selectedPoints.clear();

                    b++;
                    // Réafficher l'image initiale avec la méthode processImage
                    processImage(originalBitmap,b);

                }else {
                    Toast.makeText(MainActivity2.this, "Pas de photo séléctionnée", Toast.LENGTH_SHORT).show();
                }
            }
        });



        btnSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });

    }



    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            if (extras != null && extras.containsKey("data")) {
                Bitmap photoBitmap = (Bitmap) extras.get("data");
                if (photoBitmap != null) {
                    // Initialisez originalBitmap avec le bitmap capturé
                    originalBitmap = photoBitmap;
                } else {
                    Toast.makeText(MainActivity2.this, "Erreur lors de la capture de la photo", Toast.LENGTH_SHORT).show();
                }
            }
        }
        else if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            try {
                originalBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                // Afficher l'image choisie dans l'ImageView sans traitement
                imageView.setImageBitmap(originalBitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private void addAverageSizePoints(Mat dilatedImage) {
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(dilatedImage, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        for (MatOfPoint contour : contours) {
            for (Point point : contour.toList()) {
                Imgproc.circle(dilatedImage, point, 3, new Scalar(255, 255, 0), -1);
            }
        }
    }

    private void selectPoints(Mat lines, Mat dilatedImage) {
        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    double x = event.getX();
                    double y = event.getY();

                    double[] imagePoint = imageViewToImage(x, y);
                    Point touchedPoint = new Point(imagePoint[0], imagePoint[1]);

                    boolean isOnContour = isPointOnContour(touchedPoint, dilatedImage);

                    if (!isOnContour) {
                        Point closestContourPoint = findClosestContourPoint(touchedPoint, dilatedImage);

                        if (closestContourPoint != null && selectedPoints.size() < MAX_SELECTED_POINTS) {
                            selectedPoints.add(closestContourPoint);

                            if (selectedPoints.size() == MAX_SELECTED_POINTS) {
                                // Afficher le Toast une fois que 4 points ont été sélectionnés
                                Toast.makeText(MainActivity2.this, "4 points sélectionnés", Toast.LENGTH_SHORT).show();
                            }
                            processImage(originalBitmap,b);
                        }
                    } else {
                        addAverageSizePoints(dilatedImage);
                        processImage(originalBitmap,0);
                    }
                }
                return true;
            }
        });
    }




    private void selectPointsH(Mat lines, Mat dilatedImage) {
        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    double x = event.getX();
                    double y = event.getY();

                    double[] imagePoint = imageViewToImage(x, y);
                    Point touchedPoint = new Point(imagePoint[0], imagePoint[1]);

                    boolean isOnContour = isPointOnContour(touchedPoint, dilatedImage);

                    if (!isOnContour) {
                        Point closestContourPoint = findClosestContourPoint(touchedPoint, dilatedImage);

                        if (closestContourPoint != null && selectedPoints.size() < MAX_SELECTED_POINTS) {
                            selectedPoints.add(closestContourPoint);

                            if (selectedPoints.size() == MAX_SELECTED_POINTS) {
                                // Afficher le Toast une fois que 4 points ont été sélectionnés
                                Toast.makeText(MainActivity2.this, "4 points sélectionnés", Toast.LENGTH_SHORT).show();
                            }
                            processImageHorizontal(originalBitmap,1);
                        }
                    } else {
                        addAverageSizePoints(dilatedImage);
                        processImageHorizontal(originalBitmap,0);
                    }
                }
                return true;
            }
        });
    }




    private boolean isPointOnContour(Point point, Mat dilatedImage) {
        int neighborhoodSize = 5;
        double[] pixelValue = dilatedImage.get((int) point.y, (int) point.x);

        for (int i = -neighborhoodSize; i <= neighborhoodSize; i++) {
            for (int j = -neighborhoodSize; j <= neighborhoodSize; j++) {
                double[] currentPixel = dilatedImage.get((int) point.y + i, (int) point.x + j);
                if (currentPixel[0] != pixelValue[0]) {
                    return true;
                }
            }
        }
        return false;
    }

    private Point findClosestContourPoint(Point touchPoint, Mat dilatedImage) {
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(dilatedImage, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        Point closestContourPoint = null;
        double minContourDistance = Double.MAX_VALUE;

        for (MatOfPoint contour : contours) {
            for (Point contourPoint : contour.toList()) {
                double distance = Math.sqrt(Math.pow(contourPoint.x - touchPoint.x, 2) + Math.pow(contourPoint.y - touchPoint.y, 2));

                if (distance < minContourDistance) {
                    minContourDistance = distance;
                    closestContourPoint = contourPoint;
                }
            }
        }

        return closestContourPoint;
    }

    // Méthode pour convertir les coordonnées de l'écran en coordonnées de l'image
    private double[] imageViewToImage(double x, double y) {
        double[] imagePoint = new double[2];
        if (imageView.getDrawable() != null) {
            int viewWidth = imageView.getWidth();
            int viewHeight = imageView.getHeight();
            int imageWidth = imageView.getDrawable().getIntrinsicWidth();
            int imageHeight = imageView.getDrawable().getIntrinsicHeight();

            double scaleX = (double) imageWidth / (double) viewWidth;
            double scaleY = (double) imageHeight / (double) viewHeight;

            imagePoint[0] = x * scaleX;
            imagePoint[1] = y * scaleY;
        }
        return imagePoint;
    }

    // Méthode pour dessiner les points sélectionnés en rouge
    private void drawSelectedPoints(Mat image, List<Point> points) {


        for (Point point : points) {

            Imgproc.circle(image, point, 4, new Scalar(255, 0, 0), -1);
        }
    }



    private void processImage(Bitmap bitmap,int b) {
        somme=0;
        DecimalFormat decimalFormat = new DecimalFormat("#.###");

        double angleDroit=0l;
        double angleGauche=0l;

        // Convertir le Bitmap en Mat
        Mat imageMat = new Mat();
        Utils.bitmapToMat(bitmap, imageMat);

        // Convertir l'image en niveaux de gris
        Imgproc.cvtColor(imageMat, imageMat, Imgproc.COLOR_BGR2GRAY);

        // Appliquer un flou gaussien
        Imgproc.GaussianBlur(imageMat, imageMat, new Size(5, 5), 0);

        // Appliquer la détection de bord de Canny
        Imgproc.Canny(imageMat, imageMat, 50, 150);

        // Appliquer une dilatation
        Mat dilatedImage = new Mat();
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
        Imgproc.dilate(imageMat, dilatedImage, kernel);

        // Utiliser la transformation de Hough pour détecter les lignes
        Mat lines = new Mat();
        Imgproc.HoughLinesP(dilatedImage, lines, 1, Math.PI / 180, 50, 50, 10);

        // Appel correct à selectPoints() avec les deux arguments
        selectPoints(lines, dilatedImage);

        // Dessiner les contours sur une nouvelle image
        Mat contoursImage = new Mat(imageMat.size(), CvType.CV_8UC3, new Scalar(255, 255, 255));
        Imgproc.cvtColor(imageMat, contoursImage, Imgproc.COLOR_GRAY2BGR);



        // Dessiner les points sélectionnés en rouge
        drawSelectedPoints(contoursImage, selectedPoints);

        // Vérification que nous avons sélectionné au moins 4 points
        if (selectedPoints.size() >= 4) {
            // Première droite passant par les deux premiers points (index 0 et 1)
            Point p1 = selectedPoints.get(0);
            Point p2 = selectedPoints.get(1);

            // Deuxième droite passant par les deux derniers points (index 2 et 3)
            Point p3 = selectedPoints.get(2);
            Point p4 = selectedPoints.get(3);
            double extendLength = 1000.0; // Longueur de l'extension des droites

            // Pour la première droite
            Point extensionP1 = new Point(p1.x - extendLength, p1.y - extendLength * ((p2.y - p1.y) / (p2.x - p1.x)));
            Point extensionP2 = new Point(p2.x + extendLength, p2.y + extendLength * ((p2.y - p1.y) / (p2.x - p1.x)));

            // Pour la deuxième droite
            Point extensionP3 = new Point(p3.x - extendLength, p3.y - extendLength * ((p4.y - p3.y) / (p4.x - p3.x)));
            Point extensionP4 = new Point(p4.x + extendLength, p4.y + extendLength * ((p4.y - p3.y) / (p4.x - p3.x)));


            // Tri des points par coordonnées x
            Collections.sort(selectedPoints, new Comparator<Point>() {
                @Override
                public int compare(Point p1, Point p2) {
                    return Double.compare(p1.x, p2.x);
                }
            });

            Point leftMostPoint = selectedPoints.get(0);
            Point rightMostPoint = selectedPoints.get(0);

            for (Point point : selectedPoints) {
                if (point.x < leftMostPoint.x) {
                    leftMostPoint = point;
                }
                if (point.x > rightMostPoint.x) {
                    rightMostPoint = point;
                }
            }

            // Dessiner les lignes verticales passant par les points les plus à gauche et à droite
            Imgproc.line(contoursImage, new Point(leftMostPoint.x, 0), new Point(leftMostPoint.x, contoursImage.rows()), new Scalar(255, 0, 0), 2);
            Imgproc.line(contoursImage, new Point(rightMostPoint.x, 0), new Point(rightMostPoint.x, contoursImage.rows()), new Scalar(255, 0, 0), 2);

            // Calcul de l'intersection des lignes définies par extensionP1-extensionP2 et extensionP3-extensionP4
            Point intersectionPoint = computeIntersection(extensionP1, extensionP2, extensionP3, extensionP4);

            // Dessiner les segments limités par leftMostPoint, rightMostPoint, et l'intersection
            Imgproc.line(contoursImage, leftMostPoint, intersectionPoint, new Scalar(0, 255, 0), 2);
            Imgproc.line(contoursImage, rightMostPoint, intersectionPoint, new Scalar(0, 255, 0), 2);

            // Calcul des angles gauche et droit (angle de dépouille)
            double deltaX = Math.abs(selectedPoints.get(0).x - selectedPoints.get(1).x);
            double deltaY = Math.abs(selectedPoints.get(0).y - selectedPoints.get(1).y);
            double taperAngleRad = Math.atan(deltaY / deltaX);
            double taperAngleDeg = Math.toDegrees(taperAngleRad);
            angleGauche = 90 - taperAngleDeg;

            double deltaX2 = Math.abs(selectedPoints.get(2).x - selectedPoints.get(3).x);
            double deltaY2 = Math.abs(selectedPoints.get(2).y - selectedPoints.get(3).y);
            double taperAngleRad2 = Math.atan(deltaY2 / deltaX2);
            double taperAngleDeg2 = Math.toDegrees(taperAngleRad2);
            angleDroit = 90 - taperAngleDeg2;



            // Calcul de la pente (m) des deux droites vertes
            double m1 = (extensionP2.y - extensionP1.y) / (extensionP2.x - extensionP1.x);
            double m2 = (extensionP4.y - extensionP3.y) / (extensionP4.x - extensionP3.x);

            // Calcul de l'angle entre les deux droites (en radians)
            double intersectionAngleRad = Math.atan(Math.abs((m2 - m1) / (1 + m1 * m2)));

            // Conversion de l'angle en degrés
            double intersectionAngleDeg = Math.toDegrees(intersectionAngleRad);


            isProcessImageCalledvertical = true;

        }

        // Convertir le Mat avec les contours en Bitmap
        contoursBitmap = Bitmap.createBitmap(contoursImage.cols(), contoursImage.rows(), Bitmap.Config.ARGB_8888);
        matToBitmap(contoursImage, contoursBitmap);

        // Afficher l'image avec les contours dans l'ImageView
        imageView.setImageBitmap(contoursBitmap);

        if(b==1){
            textViewResult3.setText(Math.round(angleGauche * 10.0) / 10.0 + "");
            textViewResult4.setText(Math.round(angleDroit * 10.0) / 10.0 + "");
            imageView2.setImageBitmap(contoursBitmap);
        }else if(b==2){
            imageView3.setImageBitmap(contoursBitmap);
            textViewResult5.setText(Math.round(angleGauche * 10.0) / 10.0 + "");
            textViewResult6.setText(Math.round(angleDroit * 10.0) / 10.0 + "");
            somme=Math.round(angleGauche * 10.0) / 10.0+Math.round(angleDroit * 10.0) / 10.0;
            System.out.println(somme);
            //calcul de la note selon l'angle
            if(somme>6 && somme<16){
                note=17;
            }else if (somme>4 && somme<18){
                note=14;
            }else if(somme>2 && somme<20){
                note=12;
            }else note=10;
        }
    }

    private Point calculateIntersectionHorizontal(Point line1Start, Point line1End, Point line2Start, Point line2End) {
        double x1 = line1Start.x;
        double y1 = line1Start.y;
        double x2 = line1End.x;
        double y2 = line1End.y;
        double x3 = line2Start.x;
        double y3 = line2Start.y;
        double x4 = line2End.x;
        double y4 = line2End.y;
        double determinant = ((x1 - x2) * (y3 - y4)) - ((y1 - y2) * (x3 - x4));
        if (determinant == 0) {
            // Les lignes sont parallèles ou confondues, pas de point d'intersection
            return null;
        } else {
            double intersectionX = (((x1 * y2) - (y1 * x2)) * (x3 - x4) - (x1 - x2) * ((x3 * y4) - (y3 * x4))) / determinant;
            double intersectionY = (((x1 * y2) - (y1 * x2)) * (y3 - y4) - (y1 - y2) * ((x3 * y4) - (y3 * x4))) / determinant;

            return new Point(intersectionX, intersectionY);
        }
    }
    private void processImageHorizontal(Bitmap bitmap,int a) {
        double angleGaucheAvecLigne=0l;
        double angleDroitAvecLigne=0l;

        // Convertir le Bitmap en Mat
        Mat imageMat = new Mat();
        Utils.bitmapToMat(bitmap, imageMat);

        // Convertir l'image en niveaux de gris
        Imgproc.cvtColor(imageMat, imageMat, Imgproc.COLOR_BGR2GRAY);

        // Appliquer un flou gaussien
        Imgproc.GaussianBlur(imageMat, imageMat, new Size(5, 5), 0);

        // Appliquer la détection de bord de Canny
        Imgproc.Canny(imageMat, imageMat, 50, 150);

        // Appliquer une dilatation
        Mat dilatedImage = new Mat();
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
        Imgproc.dilate(imageMat, dilatedImage, kernel);

        // Utiliser la transformation de Hough pour détecter les lignes
        Mat lines = new Mat();
        Imgproc.HoughLinesP(dilatedImage, lines, 1, Math.PI / 180, 50, 50, 10);

        // Appel correct à selectPoints() avec les deux arguments
        selectPointsH(lines, dilatedImage);

        // Dessiner les contours sur une nouvelle image
        Mat contoursImage = new Mat(imageMat.size(), CvType.CV_8UC3, new Scalar(255, 255, 255));
        Imgproc.cvtColor(imageMat, contoursImage, Imgproc.COLOR_GRAY2BGR);

        // Dessiner les points sélectionnés en rouge
        drawSelectedPoints(contoursImage, selectedPoints);

        // Vérification que nous avons sélectionné au moins 4 points
        if (selectedPoints.size() >= 4) {
            // Première droite passant par les deux premiers points (index 0 et 1)
            Point p1 = selectedPoints.get(0);
            Point p2 = selectedPoints.get(1);

            // Deuxième droite passant par les deux derniers points (index 2 et 3)
            Point p3 = selectedPoints.get(2);
            Point p4 = selectedPoints.get(3);
            double extendLength = 1000.0; // Longueur de l'extension des droites

            // Pour la première droite
            Point extensionP1 = new Point(p1.x - extendLength, p1.y - extendLength * ((p2.y - p1.y) / (p2.x - p1.x)));
            Point extensionP2 = new Point(p2.x + extendLength, p2.y + extendLength * ((p2.y - p1.y) / (p2.x - p1.x)));

            // Pour la deuxième droite
            Point extensionP3 = new Point(p3.x - extendLength, p3.y - extendLength * ((p4.y - p3.y) / (p4.x - p3.x)));
            Point extensionP4 = new Point(p4.x + extendLength, p4.y + extendLength * ((p4.y - p3.y) / (p4.x - p3.x)));

            // Tri des points par coordonnées x
            Collections.sort(selectedPoints, new Comparator<Point>() {
                @Override
                public int compare(Point p1, Point p2) {
                    return Double.compare(p1.x, p2.x);
                }
            });

            // Recherche du point le plus à gauche et du point le plus à droite parmi les quatre points sélectionnés
            Point leftMostPoint = selectedPoints.get(0);
            Point rightMostPoint = selectedPoints.get(0);

            for (Point point : selectedPoints) {
                if (point.x < leftMostPoint.x) {
                    leftMostPoint = point;
                }
                if (point.x > rightMostPoint.x) {
                    rightMostPoint = point;
                }
            }

            Imgproc.line(contoursImage, leftMostPoint, rightMostPoint, new Scalar(0, 0, 255), 2);
            Point intersectionPoint = calculateIntersectionHorizontal(extensionP1, extensionP2, extensionP3, extensionP4);
            if (intersectionPoint != null) {
                // Dessiner des segments limités par leftMostPoint, rightMostPoint et le point d'intersection
                Imgproc.line(contoursImage, leftMostPoint, intersectionPoint, new Scalar(255, 0, 0), 2);
                Imgproc.line(contoursImage, rightMostPoint, intersectionPoint, new Scalar(255, 0, 0), 2);
            }
            // Calcul des angles avec la ligne horizontale
            angleGaucheAvecLigne = calculateAngleWithHorizontalLine(leftMostPoint, rightMostPoint, selectedPoints.get(0), selectedPoints.get(1));
            angleDroitAvecLigne = calculateAngleWithHorizontalLine(leftMostPoint, rightMostPoint, selectedPoints.get(2), selectedPoints.get(3));

            isProcessImageCalledhorizontal = true;

        }


        contoursBitmap = Bitmap.createBitmap(contoursImage.cols(), contoursImage.rows(), Bitmap.Config.ARGB_8888);
        matToBitmap(contoursImage, contoursBitmap);

        // Afficher l'image avec les contours dans l'ImageView
        imageView.setImageBitmap(contoursBitmap);

        if(a==1){
            imageView1.setImageBitmap(contoursBitmap);
            textViewResult1.setText(Math.round(angleGaucheAvecLigne * 10.0) / 10.0 + "");
            textViewResult2.setText(Math.round(angleDroitAvecLigne * 10.0) / 10.0 + "");
        }
 }

    private double calculateAngleWithHorizontalLine(Point lineStart, Point lineEnd, Point point1, Point point2) {
        double mLine = (lineEnd.y - lineStart.y) / (lineEnd.x - lineStart.x);
        double mSegment = (point2.y - point1.y) / (point2.x - point1.x);

        // Calcul de l'angle entre la ligne horizontale et le segment
        double angleRad = Math.atan(Math.abs((mSegment - mLine) / (1 + mLine * mSegment)));

        // Conversion de l'angle en degrés
        return Math.toDegrees(angleRad);
    }



    // Méthode pour calculer l'intersection de deux lignes
    private Point computeIntersection(Point p1, Point p2, Point p3, Point p4) {
        double x1 = p1.x, y1 = p1.y;
        double x2 = p2.x, y2 = p2.y;
        double x3 = p3.x, y3 = p3.y;
        double x4 = p4.x, y4 = p4.y;

        double d = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
        if (d == 0) {
            return new Point(-1, -1); // Aucune intersection
        } else {
            double x = ((x3 - x4) * (x1 * y2 - y1 * x2) - (x1 - x2) * (x3 * y4 - y3 * x4)) / d;
            double y = ((y3 - y4) * (x1 * y2 - y1 * x2) - (y1 - y2) * (x3 * y4 - y3 * x4)) / d;
            return new Point(x, y);
        }
    }

    // Envoyer les donnees au serveur
    @Override
    public void onClick(View v) {
        String endpoint = "/etudiant/saveStudentPW";
        String url = APIConfig.BASE_URL + endpoint;
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("internes", "( "+textViewResult1.getText().toString()+" ; "+textViewResult2.getText().toString()+" )");
            jsonBody.put("externes", "( "+textViewResult3.getText().toString()+" ; "+textViewResult4.getText().toString()+" )");
            jsonBody.put("depouilles", "( "+textViewResult5.getText().toString()+" ; "+textViewResult6.getText().toString()+" )");

            // Convertissez le Bitmap en chaîne Base64
            String base64Image1 = bitmapToBase64(((BitmapDrawable) imageView1.getDrawable()).getBitmap());
            String base64Image2 = bitmapToBase64(((BitmapDrawable) imageView2.getDrawable()).getBitmap());
            String base64Image3 = bitmapToBase64(((BitmapDrawable) imageView3.getDrawable()).getBitmap());

            // Ajoutez la chaîne Base64 à votre objet JSON
            jsonBody.put("image1", base64Image1);
            jsonBody.put("image2", base64Image2);
            jsonBody.put("image3", base64Image3);
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            java.text.SimpleDateFormat timeFormat = new java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault());

            String currentDate = dateFormat.format(calendar.getTime());
            String currentTime = timeFormat.format(calendar.getTime());

            // Put date and time in the JSON body
            jsonBody.put("date", currentDate);
            jsonBody.put("time", currentTime);
            jsonBody.put("note", note);
            JSONObject studentobject = new JSONObject();
            studentobject.put("id",Integer.parseInt(student));
            jsonBody.put("student",studentobject);
            JSONObject pwobject=new JSONObject();
            pwobject.put("id",Integer.parseInt(pw));
            jsonBody.put("pw",pwobject);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    jsonBody,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Toast.makeText(MainActivity2.this, "Tp soummis avec succes", Toast.LENGTH_SHORT).show();
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(MainActivity2.this, "Erreur de soumission", Toast.LENGTH_SHORT).show();
                        }
                    }
            );


            Volley.newRequestQueue(this).add(request);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // methode pour convertir un bitmap en base64
    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }
}