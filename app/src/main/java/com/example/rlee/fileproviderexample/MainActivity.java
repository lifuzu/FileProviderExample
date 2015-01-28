package com.example.rlee.fileproviderexample;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Message;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;


public class MainActivity extends ActionBarActivity {

    // LogCat tag
    private static final String TAG = MainActivity.class.getSimpleName();

    // Upload function
    private Button uploadButton;
    private ProgressBar progressBar;
    private TextView txtPercentage;
    private ImageView imgPreview;
    private VideoView vidPreview;
    long totalSize = 0;

    // Progress Bar and update handler
    ProgressDialog progressDialog;
    Handler handler;
    private static final int DOWNLOAD_COMPLETED = 0;


//    String uploadServerUri = "http://localhost:8888/?json_route=/media";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressDialog = new ProgressDialog(MainActivity.this);
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case DOWNLOAD_COMPLETED:
                        startDisplay();
                        break;
                }
            }
        };

        // Upload function
        txtPercentage = (TextView) findViewById(R.id.txtPercentage);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        imgPreview = (ImageView) findViewById(R.id.imgPreview);
        vidPreview = (VideoView) findViewById(R.id.videoPreview);
        uploadButton = (Button) findViewById(R.id.btnUpload);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // uploading the file to server
                new UploadFileToServer().execute();
            }

//        uploadButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                progressDialog = progressDialog.show(MainActivity.this, "Upload", "Uploading file...", true);
//
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                uploadButton.setText("Uploading started ...");
//                            }
//                        });
//                        uploadFile("abc.jpg", uploadServerUri);
//                    }
//                }).start();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void startDownload(View view) {

        progressDialog.setTitle("Downloading Image ...");
        progressDialog.setMessage("Download in progress ...");
        progressDialog.setProgressStyle(progressDialog.STYLE_HORIZONTAL);
        progressDialog.setProgress(0);
        progressDialog.setMax(100);
        progressDialog.show();
//
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    // Here you should write your time consuming task...
//                    while (progressDialog.getProgress() <= progressDialog.getMax()) {
//                        Thread.sleep(2000);
//                        updateHandler.post(new Runnable() {
//                            public void run() {
//                                progressDialog.incrementProgressBy(2);
//                            }
//                        });
//
//                        if (progressDialog.getProgress() == progressDialog.getMax()) {
//                            progressDialog.dismiss();
//                        }
//                    }
//                } catch (Exception e) {
//                }
//            }
//        }).start();
        download("https://farm8.staticflickr.com/7480/15854857509_98b44827c9_o_d.jpg", "abc.jpg");
        //download("http://farm1.static.flickr.com/114/298125983_0e4bf66782_b.jpg", "abc.jpg");


    }

    protected void download(final String strUrl, final String fileName) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                File file = new File(getFilesDir(), fileName);

                try {
                    URL url = new URL(strUrl);
                    Log.i("FILE_NAME", "File name is " + fileName);
                    Log.i("FILE_URL", "File URL is " + url);
                    URLConnection connection = url.openConnection();
                    connection.connect();
                    // get the file length
                    int fileLength = connection.getContentLength();

                    // download the file
                    InputStream input = new BufferedInputStream(url.openStream());
                    OutputStream output = new FileOutputStream(file);

                    byte data[] = new byte[1024];
                    long total = 0;
                    int count;
                    while ((count = input.read(data)) != -1) {
                        total += count;
                        // increase progress bar here
                        progressDialog.setProgress((int)(total*100)/fileLength);

                        // write data into file
                        output.write(data, 0, count);
                    }

                    // flush output and close streams
                    output.flush();
                    output.close();
                    input.close();
                    // send message to launch Reader
                    Message msg = Message.obtain();
                    msg.what = DOWNLOAD_COMPLETED;
                    handler.sendMessage(msg);
                    // dismiss the progress bar
                    progressDialog.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i("ERROR ON DOWNLOADING FILES", "ERROR IS" + e);
                }
            }
        }).start();
    }

    public void startDisplay() {
        // show jpg in gallery
        File file = new File(getFilesDir(), "abc.jpg");
        Uri fileUri = FileProvider.getUriForFile(this, "com.example.rlee.fileproviderexample.fileprovider", file);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(fileUri, "image/*");
        intent.addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Log.i("FILE_URI", "File Uri is " + Uri.fromFile(file));
        startActivity(intent);
    }

    /**
     * Uploading the file to server
     * */
    private class UploadFileToServer extends AsyncTask<Void, Integer, String> {
        @Override
        protected void onPreExecute() {
            // setting progress bar to zero
            progressBar.setProgress(0);
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            // Making progress bar visible
            progressBar.setVisibility(View.VISIBLE);

            // updating progress bar value
            progressBar.setProgress(progress[0]);

            // updating percentage value
            txtPercentage.setText(String.valueOf(progress[0]) + "%");
        }

        @Override
        protected String doInBackground(Void... params) {
            return uploadFile();
        }

        @SuppressWarnings("deprecation")
        private String uploadFile() {
            String responseString = null;

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(Config.FILE_UPLOAD_URL);

            try {
                AndroidMultiPartEntity entity = new AndroidMultiPartEntity(
                        new AndroidMultiPartEntity.ProgressListener() {

                            @Override
                            public void transferred(long num) {
                                publishProgress((int) ((num / (float) totalSize) * 100));
                            }
                        });

//                MultipartEntity entity = new MultipartEntity();
//                File sourceFile = new File(filePath);
                File sourceFile = new File(getFilesDir(), "abc.jpg");

//                entity.addPart("file", new FileBody(sourceFile, "image/jpeg"));
                // Adding file data to http body
                entity.addPart("image", new FileBody(sourceFile));

                // Extra parameters if you want to pass to server
//                entity.addPart("website",
//                        new StringBody("www.weimed.com"));
//                entity.addPart("email", new StringBody("abc@gmail.com"));

                totalSize = entity.getContentLength();
                httppost.setEntity(entity);

                // Making server call
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity r_entity = response.getEntity();

                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    // Server response
                    responseString = EntityUtils.toString(r_entity);
                } else {
                    responseString = "Error occurred! Http Status Code: "
                            + statusCode;
                }

            } catch (ClientProtocolException e) {
                responseString = e.toString();
            } catch (IOException e) {
                responseString = e.toString();
            }

            return responseString;

        }

        @Override
        protected void onPostExecute(String result) {
            Log.e(TAG, "Response from server: " + result);

            // showing the server response in an alert dialog
            showAlert(result);

            super.onPostExecute(result);
        }

    }

    /**
     * Method to show alert dialog
     * */
    private void showAlert(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message).setTitle("Response from Servers")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // do nothing
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
