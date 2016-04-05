package la.oja.senseware;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrador on 28-01-2016.
 */
public class ResultListAdapter extends ArrayAdapter<String> {

    private final Activity context;
    private final List<Map<String, String> >results;
    private final Typeface font;
    private final String[] answer;


    public ResultListAdapter(Activity context, String[] answer, Typeface font, List<Map<String, String> >results) {
        super(context, R.layout.list_my_history_row, answer);

        this.context = context;
        this.font = font;
        this.answer = answer;
        this.results = results;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater=context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.list_my_history_row, null, true);

       TextView date = (TextView) rowView.findViewById(R.id.date);
        TextView question = (TextView) rowView.findViewById(R.id.question);
        TextView answer = (TextView) rowView.findViewById(R.id.answer);

        date.setTypeface(font);
        question.setTypeface(font);
        answer.setTypeface(font);

        date.setText(results.get(position).get("date"));
        question.setText(results.get(position).get("question"));
        answer.setText(results.get(position).get("answer"));


        ImageButton edit = (ImageButton) rowView.findViewById(R.id.edit);
/*
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(context, EditResultActivity.class);
                intent.putExtra("_ID", results.get(position).get("_ID"));
                intent.putExtra("answer", results.get(position).get("answer"));
                intent.putExtra("question", results.get(position).get("question"));

                context.startActivity(intent);
                context.finish();

            }
        });

        */

        return rowView;
    }
}
