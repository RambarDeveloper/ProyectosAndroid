package la.oja.senseware;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import la.oja.senseware.R;

/**
 * Created by Administrador on 19-01-2016.
 */
public class CountryAdapter extends ArrayAdapter<Country> {

    private LayoutInflater mLayoutInflater;

    public CountryAdapter(Context context) {
        super(context, 0);
        mLayoutInflater = LayoutInflater.from(context);
    }

    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.item_country_drop, parent, false);
            holder = new ViewHolder();
            holder.mImageView = (ImageView) convertView.findViewById(R.id.image);
            holder.mNameView = (TextView) convertView.findViewById(R.id.country_name);
            holder.mCodeView = (TextView) convertView.findViewById(R.id.country_code);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Country country = getItem(position);
        if (country != null) {
            holder.mNameView.setText(country.getName());
            holder.mCodeView.setText(country.getCountryCodeStr());
            holder.mImageView.setImageResource(country.getResId());
        }
        return convertView;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        Country country = getItem(position);
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.item_country, null);
        }
        ImageView imageView = (ImageView) convertView.findViewById(R.id.image);
        imageView.setImageResource(country.getResId());
        return convertView;
    }

    private static class ViewHolder {
        public ImageView mImageView;
        public TextView mNameView;
        public TextView mCodeView;
    }
}
