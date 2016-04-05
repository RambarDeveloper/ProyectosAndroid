package la.oja.senseware;

/**
 * Created by Juan Robles on 23/11/2015.
 */

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Collections;

import la.oja.senseware.R;

class customListAdapter extends ArrayAdapter<String> {

    private final Activity context;
    private final String[] itemname;
    private final Typeface font;

    public customListAdapter(Activity context, String[] itemname, Typeface font) {
        super(context, R.layout.list_row, itemname);
        // TODO Auto-generated constructor stub

        this.context=context;
        this.itemname=itemname;
        this.font=font;
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.list_row, null,true);

        TextView txtTitle = (TextView) rowView.findViewById(R.id.item);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
        TextView extratxt = (TextView) rowView.findViewById(R.id.textView1);

        txtTitle.setTypeface(font);
        extratxt.setTypeface(font);

        txtTitle.setText(itemname[position]);

        SharedPreferences settings = context.getSharedPreferences("ActivitySharedPreferences_data", 0);
        int current = settings.getInt("current", 0);

        if(position < (current-1) || current == -1)
        {
            imageView.setImageResource(R.mipmap.check);
        }
        else if(position==(current-1))
        {
            imageView.setImageResource(R.mipmap.play);
        }
        else
        {
            imageView.setImageResource(R.mipmap.lock);
        }
        //int id = getResources().getIdentifier("@android:drawable/ic_media_pause", null, null);
        //imageView.setImageResource(id);
        extratxt.setText("Actividad "+(position+1));
        return rowView;

    }

}