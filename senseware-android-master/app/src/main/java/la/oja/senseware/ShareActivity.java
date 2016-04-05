package la.oja.senseware;

import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.File;
import java.util.List;

public class ShareActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        Typeface ultralight = Typeface.createFromAsset(getAssets(), "fonts/SF-UI-Display-Ultralight.ttf");
        Typeface light = Typeface.createFromAsset(getAssets(), "fonts/SF-UI-Text-Light.ttf");
        Typeface thin = Typeface.createFromAsset(getAssets(), "fonts/SF-UI-Display-Thin.ttf");
        Typeface regular = Typeface.createFromAsset(getAssets(), "fonts/SF-UI-Text-Regular.ttf");

        TextView title = (TextView) findViewById(R.id.title);
        title.setTypeface(regular);

        TextView title2 = (TextView) findViewById(R.id.title2);
        title.setTypeface(light);

        TextView title3 = (TextView) findViewById(R.id.title3);
        title.setTypeface(light);

        Button btnFacebook = (Button) findViewById(R.id.btnFacebook);
        btnFacebook.setTypeface(thin);

        Button btnTwitter = (Button) findViewById(R.id.btnTwitter);
        btnTwitter.setTypeface(thin);

        ImageButton close = (ImageButton) findViewById(R.id.close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               ShareActivity.this.finish();

            }
        });

        btnTwitter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initShareIntent("twi", "Aprende cómo concretar tus ideas, 10 minutos al día con #Senseware http://ojalab.com/senseware?utm_campaign=twandroid&utm_source=twitter");

            }
        });

        btnFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initShareIntent("facebook", "Aprende cómo concretar tus ideas, 10 minutos al día con Senseware http://ojalab.com/senseware?utm_campaign=fbandroid&utm_source=facebook");
            }
        });
    }

    private void initShareIntent(String type, String msg) {
        boolean found = false;
        Intent share = new Intent(android.content.Intent.ACTION_SEND);
        share.setType("text/plain");

        // gets the list of intents that can be loaded.
        List<ResolveInfo> resInfo = getPackageManager().queryIntentActivities(share, 0);
        if (!resInfo.isEmpty()){
            for (ResolveInfo info : resInfo) {
                if ( info.activityInfo.name.toLowerCase().contains(type) ) {
                    share.putExtra(android.content.Intent.EXTRA_SUBJECT, "Senseware");
                    share.putExtra(android.content.Intent.EXTRA_TEXT, msg);

                    // share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(myPath)) ); // Optional, just if you wanna share an image.
                    final ActivityInfo activity = info.activityInfo;
                    final ComponentName name = new ComponentName(activity.applicationInfo.packageName, activity.name);

                    share.setPackage(info.activityInfo.packageName);
                    share.setComponent(name);
                    startActivity(share);
                    found = true;
                    break;
                }
            }
            if (!found)
                return;

            startActivity(Intent.createChooser(share, "Select"));
        }
    }
}
