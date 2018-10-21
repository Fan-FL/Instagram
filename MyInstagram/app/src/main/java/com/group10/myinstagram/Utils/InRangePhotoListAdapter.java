package com.group10.myinstagram.Utils;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.group10.myinstagram.Models.InRangePhoto;
import com.group10.myinstagram.Models.User;
import com.group10.myinstagram.R;

import java.util.List;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import de.hdodenhof.circleimageview.CircleImageView;

public class InRangePhotoListAdapter extends ArrayAdapter<InRangePhoto> {

    private static final String TAG = "InRangePhotoListAdapter";

    private LayoutInflater mInflater;
    private List<InRangePhoto> mPhotos = null;
    private int layoutResource;
    private Context mContext;

    public InRangePhotoListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<InRangePhoto> objects) {
        super(context, resource, objects);
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutResource = resource;
        this.mPhotos = objects;
    }

    private static class ViewHolder{
        SquareImageView inrangeImage;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Log.d(TAG, "getView: load inrange image.");


        final InRangePhotoListAdapter.ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(layoutResource, parent, false);
            holder = new InRangePhotoListAdapter.ViewHolder();

            holder.inrangeImage = (SquareImageView) convertView.findViewById(R.id.inrange_image);

            convertView.setTag(holder);
        } else {
            holder = (InRangePhotoListAdapter.ViewHolder) convertView.getTag();
        }

        holder.inrangeImage.setImageBitmap(getItem(position).getBitmap());

        return convertView;
    }
}
