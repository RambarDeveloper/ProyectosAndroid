package rambar.com.asynctask;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button sleepButton = (Button) findViewById(R.id.sleepButton);

        sleepButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(int i=0; i<10; i++){
                    segundo();
                }
            }
        });

        Button threadButton = (Button) findViewById(R.id.threadButton);

        threadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                thread();
            }
        });

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setMax(99);
        progressBar.setProgress(0);

        final Button asyncButton = (Button) findViewById(R.id.asyncButton);

        asyncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EjemploAsyncTask asyncTask = new EjemploAsyncTask();
                asyncTask.execute();
            }
        });

    }

    private void thread(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                for(int i = 0; i < 10 ; i++){
                    segundo();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getBaseContext(), "Thread Message", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).start();
    }

    private void segundo(){
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private class EjemploAsyncTask extends AsyncTask<Void, Integer, Boolean>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setMax(100);
            progressBar.setProgress(0);
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            for(int i=0; i<10; i++){
                segundo();
                publishProgress(i*10);
                if (isCancelled()){
                    break;
                }
            }

            return true;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressBar.setProgress(values[0].intValue());
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (aBoolean){
                Toast.makeText(getBaseContext(), "Async Task finalizada ", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            Toast.makeText(getBaseContext(), "Async Task cancelada ", Toast.LENGTH_SHORT).show();
        }

    }
}
