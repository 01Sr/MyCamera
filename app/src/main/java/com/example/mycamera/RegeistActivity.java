package com.example.mycamera;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class RegeistActivity extends AppCompatActivity {
    EditText nameText;
    Button  regeistBtn;
    ProgressBar mProgressView;
    final String URL="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regeist);
        nameText= (EditText) findViewById(R.id.name_text);
        regeistBtn= (Button) findViewById(R.id.regeist_btn);
        mProgressView= (ProgressBar) findViewById(R.id.progressBar);
        regeistBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(nameText.getText().toString().trim().equals(""))
                    nameText.setError("用户名不能为空");
                else{
                    if(mProgressView.getVisibility()==View.GONE)
                        showProgress(true);
                    RegeistTask regeistTask=new RegeistTask();
                    regeistTask.execute(URL, String.valueOf(nameText.getText()));
                }
            }
        });

    }

        class RegeistTask extends AsyncTask<String,Integer,String>{
           final int TIME=2000;

            @Override
            protected String doInBackground(String... params) {
                byte[] username=params[1].getBytes();
                try {
                    java.net.URL url=new URL(params[0]);
                    HttpURLConnection conn= (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(TIME);
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.setUseCaches(false);
                    conn.setRequestProperty("Charset", "UTF-8");
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    conn.setRequestProperty("Content-Length",String.valueOf(username.length));
                    OutputStream os=conn.getOutputStream();
                    os.write(username);
                    os.close();
                    int code=conn.getResponseCode();
                    if(code==200){
                      return  getResponse(conn);
                    }else{
                        return "-2";
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                if(mProgressView.getVisibility()==View.VISIBLE)
                    showProgress(false);
                showProgress(false);
                switch (Integer.parseInt(s)){
                    case -2:Toast.makeText(RegeistActivity.this,"网络连接错误",Toast.LENGTH_SHORT);break;
                    case 0:nameText.setError("用户名已被使用!");break;
                    case 1:
                        Intent intent=new Intent(RegeistActivity.this,ShowNameActivity.class);
                        intent.putExtra("name",nameText.getText());
                        startActivity(intent);
                }

            }
        }

    String getResponse(HttpURLConnection conn) throws IOException {

        InputStream is = null;
        try {
            is = conn.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        InputStreamReader isr = null;
        try {
            isr = new InputStreamReader(is, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        BufferedReader br = new BufferedReader(isr);
        String line;
        String result = new String();
        while ((line = br.readLine()) != null)
            result += line;
        return result;
    }


    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            nameText.setVisibility(show ? View.GONE : View.VISIBLE);
            nameText.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    nameText.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            regeistBtn.setVisibility(show ? View.GONE : View.VISIBLE);
            regeistBtn.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    regeistBtn.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            nameText.setVisibility(show ? View.GONE : View.VISIBLE);
            regeistBtn.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

}
