package vikashkumar.instagramshare;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.print.PrintHelper;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private Menu menu;
    private EditText userUrl;
    private TextInputLayout inputLayoutUrl;
    private ProgressDialog bar;
    private CardView imageShowLayout,videoShowLayout;
    private ImageView image;
    private ImageButton printImage,downloadImage,shareImage,downloadVideo,shareVideo,playVideo;
    private VideoView video;
    private String url;
    private File downloadFileName,cache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        //It will ignore URI exposure
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        inputLayoutUrl = (TextInputLayout) findViewById(R.id.input_layout_url);
        userUrl = (EditText) findViewById(R.id.user_url);
        userUrl.addTextChangedListener(new MyTextWatcher(userUrl));
        videoShowLayout=(CardView)findViewById(R.id.videoShowLayout);
        imageShowLayout=(CardView)findViewById(R.id.imageShowLayout);

        printImage=(ImageButton) findViewById(R.id.printImage);
        downloadImage=(ImageButton) findViewById(R.id.downloadImage);
        shareImage=(ImageButton) findViewById(R.id.shareImage);
        downloadVideo=(ImageButton) findViewById(R.id.downloadVideo);
        shareVideo=(ImageButton) findViewById(R.id.shareVideo);
        playVideo=(ImageButton) findViewById(R.id.playVideo);

        image=(ImageView)findViewById(R.id.image);
        video=(VideoView) findViewById(R.id.video);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String apppackage = "com.instagram.android";
                try {
                    Intent i = getPackageManager().getLaunchIntentForPackage(apppackage);
                    startActivity(i);
                } catch (Exception  e) {
                    Toast.makeText(MainActivity.this, "Sorry, Instagram App Not Found", Toast.LENGTH_LONG).show();
                }
            }
        });

        AppBarLayout mAppBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
        mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    isShow = true;
                    showOption(R.id.instagram);
                } else if (isShow) {
                    isShow = false;
                    hideOption(R.id.instagram);
                }
            }
        });
    }

    public void clearEditBox(View v) {
        hideKeyboard();
        if (!(userUrl.getText().toString().isEmpty()))
            userUrl.setText("");
        else inputLayoutUrl.setError("Nothing to be cleared !!");
    }

    public void pasteURL(View v) {
        hideKeyboard();
        userUrl.setText("");
        ClipboardManager clipBoard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = clipBoard.getPrimaryClip();
        ClipData.Item item = clipData.getItemAt(0);
        String clipURL = item.getText().toString();
        if (clipURL.startsWith("https://www.instagram.com")) userUrl.setText(clipURL + "");
        else{
            Snackbar.make(v, "No Instagram Url Found !!", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }

    }

    public void loadURL(View v) {
        hideKeyboard();
        if (!validateUrl()) {
            return;
        }
        bar=new ProgressDialog(MainActivity.this);
        bar.setTitle("Connecting server");
        bar.setMessage("Please Wait ... ");
        bar.setCancelable(false);
        bar.show();
        getUrlFromSourceCode();

    }
    private void getUrlFromSourceCode() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final StringBuilder builder = new StringBuilder();
                try {
                    Document doc = Jsoup.connect(userUrl.getText().toString().trim()).get();
                    Elements links = doc.select("meta[property=og:video]");
                    //checking whether it is video or image
                    if (links.isEmpty())
                    {
                        links = doc.select("meta[property=og:image]");
                        for (Element link : links) {
                            builder.append(link.attr("content"));
                        }
                    }else{
                        for (Element link : links) {
                            builder.append(link.attr("content"));
                        }
                    }
                } catch (IOException e) {
                }
                url=builder.toString().trim();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(bar.isShowing()) {
                            imageShowLayout.setVisibility(View.GONE);
                            videoShowLayout.setVisibility(View.GONE);
                            if (builder.toString().contains(".jpg")){
                                imageShowLayout.setVisibility(View.VISIBLE);
                                Glide.with(MainActivity.this).load(builder.toString()).fitCenter().into(image);
                                try{
                                    Thread.sleep(2000);
                                }catch (Exception e){}

                            }else{
                                Uri uri = Uri.parse(builder.toString());
                                video.setVideoURI(uri);
                                video.requestFocus();
                                videoShowLayout.setVisibility(View.VISIBLE);
                                try{
                                    Thread.sleep(5000);
                                }catch (Exception e){}

                            }
                        }
                        bar.dismiss();
                    }

                });
            }
        }).start();
    }
    public void startPlayingVideo(View view){
        if (video.getDuration()==-1)
            Toast.makeText(MainActivity.this, "Nothing to play", Toast.LENGTH_SHORT).show();
        else
            video.start();
    }
    public void pausePlayingVideo(View view){
        if (video.isPlaying())
            video.pause();
    }
    public void downloadVideoToSDCard(View view){
        final ProgressDialog downloadingBar=new ProgressDialog(MainActivity.this);
        downloadingBar.setTitle("Please wait");
        downloadingBar.setMessage("Video is downloading to Internal Memory ... ");
        downloadingBar.setCancelable(false);
        downloadingBar.show();
        SimpleDateFormat sd = new SimpleDateFormat("yymmhh");
        String date = sd.format(new Date());
        String name = "InstagramShare" + date + ".mp4";
        cache = getApplicationContext().getExternalCacheDir();
        downloadFileName = new File(cache, name);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL u = new URL(url);
                    URLConnection conn = u.openConnection();
                    int contentLength = conn.getContentLength();

                    DataInputStream stream = new DataInputStream(u.openStream());

                    byte[] buffer = new byte[contentLength];
                    stream.readFully(buffer);
                    stream.close();

                    DataOutputStream fos = new DataOutputStream(new FileOutputStream(downloadFileName));
                    fos.write(buffer);
                    fos.flush();
                    fos.close();
                    downloadingBar.dismiss();

                } catch(FileNotFoundException e) {
                    Toast.makeText(MainActivity.this, "Error !! Try Again...", Toast.LENGTH_SHORT).show();
                    return; // swallow a 404
                } catch (IOException e) {
                    Toast.makeText(MainActivity.this, "Error !! Try Again...", Toast.LENGTH_SHORT).show();
                    return; // swallow a 404
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                        alertDialogBuilder.setTitle("Video is downloaded to Internal Storage. ");
                        alertDialogBuilder.setMessage("Want to Delete/Open File ?");
                        alertDialogBuilder.setPositiveButton("OPEN", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                Uri mydir = Uri.parse("file://"+downloadFileName);
                                intent.setDataAndType(mydir,"video/*");    // or use */*
                                startActivity(intent);

                            }
                        });
                        alertDialogBuilder.setNegativeButton("DELETE",new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (downloadFileName.exists())
                                    downloadFileName.delete();
                                Toast.makeText(MainActivity.this, "Video is deleted", Toast.LENGTH_SHORT).show();
                            }
                        });
                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                    }
                });
            }
        }).start();

    }
    public void shareVideo(View view){
        final ProgressDialog downloadingBar=new ProgressDialog(MainActivity.this);
        downloadingBar.setTitle("Please wait");
        downloadingBar.setMessage("Just a second the video is avialable to be shared..");
        downloadingBar.setCancelable(false);
        downloadingBar.show();

        cache = getApplicationContext().getExternalCacheDir();
        downloadFileName = new File(cache, "InstagramShare.mp4");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL u = new URL(url);
                    URLConnection conn = u.openConnection();
                    int contentLength = conn.getContentLength();

                    DataInputStream stream = new DataInputStream(u.openStream());

                    byte[] buffer = new byte[contentLength];
                    stream.readFully(buffer);
                    stream.close();

                    DataOutputStream fos = new DataOutputStream(new FileOutputStream(downloadFileName));
                    fos.write(buffer);
                    fos.flush();
                    fos.close();
                    downloadingBar.dismiss();
                } catch(FileNotFoundException e) {
                    Toast.makeText(MainActivity.this, "Error !! Try Again...", Toast.LENGTH_SHORT).show();
                    return; // swallow a 404
                } catch (IOException e) {
                    Toast.makeText(MainActivity.this, "Error !! Try Again...", Toast.LENGTH_SHORT).show();
                    return; // swallow a 404
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        Intent share = new Intent(android.content.Intent.ACTION_SEND);
                        share.setType("video/*");
                        share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + downloadFileName));
                        share.putExtra(Intent.EXTRA_TEXT, "Hey Want to share & Print Photos/video from Instagram.Check this application " + "https://play.google.com/store/apps/details?id=" + getPackageName());
                        startActivity(Intent.createChooser(share, "Share This Video with"));
                    }
                });
            }
        }).start();
    }
    public void PrintImage(View view){
        if (image.getDrawable() != null) {
            GlideBitmapDrawable bitmapDrawable = (GlideBitmapDrawable) image.getDrawable();
            Bitmap bitmap = bitmapDrawable.getBitmap();
            // Save this bitmap to a file.
            File cache = getApplicationContext().getExternalCacheDir();
            File sharefile = new File(cache, "InstagramShare.png");
            try {
                FileOutputStream out = new FileOutputStream(sharefile);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                out.flush();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            PrintHelper photoPrinter = new PrintHelper(MainActivity.this);
            photoPrinter.setScaleMode(PrintHelper.SCALE_MODE_FIT);
            photoPrinter.printBitmap("InstagramShare", bitmap);
            sharefile.delete();
        } else
            Toast.makeText(MainActivity.this, "No Image to print", Toast.LENGTH_SHORT).show();
    }
    public void DownloadImage(View view){
        SimpleDateFormat sd = new SimpleDateFormat("yymmhh");
        String date = sd.format(new Date());
        String name = "InstagramShare" + date + ".png";
        cache = getApplicationContext().getExternalCacheDir();
        downloadFileName = new File(cache, name);

        if (image.getDrawable() != null) {

            GlideBitmapDrawable bitmapDrawable = (GlideBitmapDrawable) image.getDrawable();
            Bitmap bitmap = bitmapDrawable.getBitmap();
            try {
                FileOutputStream out = new FileOutputStream(downloadFileName);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                out.flush();
                out.close();
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setTitle("Image is downloaded to Internal Storage. ");
                alertDialogBuilder.setMessage("Want to Delete/Open File ?");
                alertDialogBuilder.setPositiveButton("OPEN", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        Uri mydir = Uri.parse("file://"+downloadFileName);
                        intent.setDataAndType(mydir,"image/*");    // or use */*
                        startActivity(intent);

                        }
                });
                alertDialogBuilder.setNegativeButton("DELETE",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (downloadFileName.exists())
                            downloadFileName.delete();
                        Toast.makeText(MainActivity.this, "Image is deleted", Toast.LENGTH_SHORT).show();
                    }
                });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else
            Toast.makeText(MainActivity.this, "No Image to Download ..", Toast.LENGTH_SHORT).show();


    }
    public void ShareImage(View view){
        if (image.getDrawable() != null) {
            GlideBitmapDrawable bitmapDrawable = (GlideBitmapDrawable) image.getDrawable();
            Bitmap bitmap = bitmapDrawable.getBitmap();
            // Save this bitmap to a file.
            File cache = getApplicationContext().getExternalCacheDir();
            File sharefile = new File(cache, "InstagramShare.png");
            try {
                FileOutputStream out = new FileOutputStream(sharefile);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                out.flush();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Intent share = new Intent(android.content.Intent.ACTION_SEND);
            share.setType("image/*");
            share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + sharefile));
            share.putExtra(Intent.EXTRA_TEXT, "Hey Want to share & Print Photo and video from Instagram.Check this application " + "https://play.google.com/store/apps/details?id=" + getPackageName());
            startActivity(Intent.createChooser(share, "Share This Image with"));

        } else
            Toast.makeText(MainActivity.this, "There is no image to share", Toast.LENGTH_SHORT).show();
    }
    private boolean validateUrl() {
        if (userUrl.getText().toString().trim().isEmpty()) {
            inputLayoutUrl.setError("Field can't be empty !!");
            requestFocus(userUrl);
            return false;
        }else if(!userUrl.getText().toString().trim().contains("https://www.instagram.com/")){
            inputLayoutUrl.setError("Please Check the url !!");
            requestFocus(userUrl);
            return false;
        }else {
            inputLayoutUrl.setErrorEnabled(false);
        }
        return true;
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }
    private class MyTextWatcher implements TextWatcher {

        private View view;

        private MyTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void afterTextChanged(Editable editable) {
            switch (view.getId()) {
                case R.id.user_url:
                    validateUrl();
                    break;
            }
        }
    }
    private void hideKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        } catch(Exception e){}
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);
        hideOption(R.id.instagram);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.howToUse) {
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            TextView textView = new TextView(MainActivity.this);
            textView.setText("HOW TO USE ?");
            textView.setPadding(20, 30, 20, 30);
            textView.setTextSize(20F);
            textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
            textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.instagram, 0, 0, 0);
            textView.setCompoundDrawablePadding(10);
            textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
            textView.setBackgroundResource(R.drawable.toolbar_background);
            textView.setTextColor(Color.WHITE);
            alertDialogBuilder.setCustomTitle(textView);
            alertDialogBuilder.setIcon(R.mipmap.ic_launcher);
            String text = getString(R.string.how_to_use);
            Spanned styledText = Html.fromHtml(text);
            alertDialogBuilder.setMessage(styledText);
            alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                   alertDialogBuilder.create().dismiss();
                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
            return true;
        } else if (id == R.id.instagram) {
            String apppackage = "com.instagram.android";
            try {
                Intent i = getPackageManager().getLaunchIntentForPackage(apppackage);
                startActivity(i);
            } catch (Exception  e) {
                Toast.makeText(this, "Sorry, Instagram Apps Not Found", Toast.LENGTH_LONG).show();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void hideOption(int id) {
        MenuItem item = menu.findItem(id);
        item.setVisible(false);
    }

    private void showOption(int id) {
        MenuItem item = menu.findItem(id);
        item.setVisible(true);
    }
}