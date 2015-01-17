package com.example.rlee.fileproviderexample;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;


public class MainActivity extends ActionBarActivity {

    // Progress Bar and update handler
    ProgressDialog progressDialog;
    Handler updateHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressDialog = new ProgressDialog(MainActivity.this);
        updateHandler = new Handler();
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
                    // dismiss the progress bar
                    progressDialog.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i("ERROR ON DOWNLOADING FILES", "ERROR IS" + e);
                }
            }
        }).start();
    }

    public void startDisplay(View view) {
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
}
