package xyz.devdiscovery.p1sndmsg;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import android.content.ActivityNotFoundException;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import au.com.bytecode.opencsv.CSVReader;
import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

public class MainActivity extends AppCompatActivity implements
        OnItemSelectedListener {

    private String bestUrl = "https://www.andr-discovery.xyz/category.xml";
    private Spinner spMainSelectCategory;
    private ArrayList<String> categoryList = new ArrayList<String>();
    private Button buttonSend;
    private EditText priceMessege;
    private EditText textMessage;

    static final int REQUEST_TAKE_PHOTO = 1;
    private String mCurrentPhotoPath;
    private ImageView imageView;
    private Uri photoURI;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);

        spMainSelectCategory = (Spinner) findViewById(R.id.selectIdProduct);
        buttonSend = (Button) findViewById(R.id.sendMailButton);
        priceMessege = (EditText) findViewById(R.id.priceTextMessege);
        textMessage = (EditText) findViewById(R.id.editTextMessege);


        List<String[]> list = new ArrayList<String[]>();
        String next[] = {};
        try {
            InputStreamReader csvStreamReader = new InputStreamReader(
                    MainActivity.this.getAssets().open(
                            "Category.csv"));

            CSVReader reader = new CSVReader(csvStreamReader);
            for (;;) {
                next = reader.readNext();
                if (next != null) {
                    list.add(next);
                } else {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (int i = 0;  i < list.size(); i++) {
            categoryList.add(list.get(i)[1]);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, categoryList);
        adapter.setDropDownViewResource(android.R.layout.simple_list_item_checked);

        spMainSelectCategory.setAdapter(adapter);
        spMainSelectCategory.setOnItemSelectedListener(this);



    }

    @Override
    public void onItemSelected(final AdapterView<?> arg0, final View arg1, final int arg2,
                               final long arg3) {
        buttonSend.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                EditText priceMessege = (EditText) findViewById(R.id.priceTextMessege);

                String message = textMessage.getText().toString();
                String price = priceMessege.getText().toString();

                Intent email = new Intent(Intent.ACTION_SEND);
                email.putExtra(Intent.EXTRA_EMAIL, new String[]{"expense@pro100systems.com.ua"});
                email.putExtra(Intent.EXTRA_SUBJECT, categoryList.get(arg2)+ " " + price);
                email.putExtra(Intent.EXTRA_TEXT, message);
                email.putExtra(Intent.EXTRA_STREAM, photoURI);

                email.setType("message/rfc822");

                email.setClassName("com.google.android.gm", "com.google.android.gm.ComposeActivityGmailExternal");
                Toast toast = Toast.makeText(getApplicationContext(), "Запуск почтового клиента GMAIL",Toast.LENGTH_LONG);
                toast.show();
                try {
                    startActivity(email);

                } catch (ActivityNotFoundException ex) {
                    // GMail not found
                }



            }
        });



    }



    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }

    public void onClick(View view) {
        dispatchTakePictureIntent();
        Toast toast = Toast.makeText(getApplicationContext(), "Делаем снимок :)",Toast.LENGTH_LONG);
        toast.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            imageView.setImageURI(photoURI);
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Toast.makeText(this, "Error!", Toast.LENGTH_SHORT).show();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.provider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    // Remount read XML file.
    private class DownloadPageTask extends
            AsyncTask<String, Void, List<String>> {

        @Override
        protected ArrayList<String> doInBackground(String... urls) {
            try {
                return downloadOneUrl(urls[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return categoryList;
        }

        @Override
        protected void onPostExecute(List<String> result) {
            spMainSelectCategory.setAdapter(new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, result));

            super.onPostExecute(result);
        }
    }
/*
    ArrayAdapter<String> adapter = new ArrayAdapter<String>(
            getApplicationContext(), android.R.layout.simple_list_item_1,
            categoryList);
        adapter.setDropDownViewResource(android.R.layout.simple_list_item_checked);

        spMainSelectCategory.setAdapter(adapter);
        spMainSelectCategory.setOnItemSelectedListener(this);
*/
    private ArrayList<String> downloadOneUrl(String myurl) throws
            Exception {
        InputStream inputStream = null;

        try {
            URL url = new URL(myurl);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setReadTimeout(100000);
            connection.setConnectTimeout(100000);
            connection.setRequestMethod("GET");
            connection.setInstanceFollowRedirects(true);
            connection.setUseCaches(false);
            connection.setDoInput(true);

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) { // 200 OK
                inputStream = connection.getInputStream();

                InputSource inputSource = new InputSource(inputStream);
                // Создаем экземпляр XPath
                XPath xpath = XPathFactory.newInstance().newXPath();
                // задаем выражение для разбора
                String expression = "//title";
                // список полученных узлов
                NodeList nodes = (NodeList) xpath.evaluate(expression,
                        inputSource, XPathConstants.NODESET);


                // если узел найден
                if (nodes != null && nodes.getLength() > 0) {
                    categoryList.clear();
                    int nodesLength = nodes.getLength();
                    for (int i = 0; i < nodesLength; ++i) {
                        // формируем списочный массив
                        Node node = nodes.item(i);
                        categoryList.add(node.getTextContent());
                    }
                }
            } else {
                String data = connection.getResponseMessage()
                        + " . Error Code : " + responseCode;
            }
            connection.disconnect();
            // return data;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return categoryList;
    }

    public void onClickSpinner(View view) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            new DownloadPageTask().execute(bestUrl);
        } else {
            Toast.makeText(this, "Нет интернета", Toast.LENGTH_SHORT).show();
        }
    }

}
