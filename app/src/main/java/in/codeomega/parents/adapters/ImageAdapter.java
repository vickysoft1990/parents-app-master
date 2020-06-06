package in.codeomega.parents.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import in.codeomega.parents.Gallery;
import in.codeomega.parents.Home;
import in.codeomega.parents.R;
import in.codeomega.parents.UploadedImages;
import in.codeomega.parents.model.Image;
import in.codeomega.parents.model.Notification;

/**
 * Created by HP on 09-Mar-18.
 */

public class ImageAdapter extends BaseAdapter implements View.OnClickListener {

    ArrayList<Image> images;
    Activity activity;
    LayoutInflater inflater;

    Image image;

    public ImageAdapter(Activity activity, ArrayList<Image> images) {
        this.activity = activity;
        this.images = images;

    }

    @Override
    public int getCount() {

        return images.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        image = images.get(position);

        if (inflater == null) {
            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        convertView = inflater.inflate(R.layout.previous_image_row, null);

        LinearLayout imageContainer = (LinearLayout) convertView.findViewById(R.id.imageContainer);
        TextView description = (TextView) convertView.findViewById(R.id.description);
        TextView title = (TextView) convertView.findViewById(R.id.title);
        TextView type = (TextView) convertView.findViewById(R.id.type);
        ImageView imageView = (ImageView) convertView.findViewById(R.id.image);
        TextView sec = (TextView) convertView.findViewById(R.id.sec);
        TextView std = (TextView) convertView.findViewById(R.id.std);
        TextView name = (TextView) convertView.findViewById(R.id.name);
        TextView youtube = (TextView) convertView.findViewById(R.id.youtube);
        ImageView userPic = (ImageView) convertView.findViewById(R.id.user_pic);

        Glide.with(activity).load(image.pic).placeholder(R.drawable.circular_user_place_holder).into(userPic);
        title.setText(image.title);
        description.setText(image.description);
        name.setText(image.name);

        youtube.setText(image.youtubelink);

        if(image.youtubelink.length()!=0)
        {
            youtube.setVisibility(View.VISIBLE);

        }else
        {
            youtube.setVisibility(View.GONE);
        }

        std.setText("STD : " + image.class_names);
        sec.setText("SEC : " + image.section_names);

        type.setText(image.imgType);

        imageContainer.setTag(position);
        imageContainer.setOnClickListener(this);

        Glide.with(activity).load(image.image).placeholder(R.drawable.ic_image_black_24dp).into(imageView);

        Log.e("image",image.image);

        return convertView;
    }

    @Override
    public void onClick(View view) {

        int position = Integer.parseInt(view.getTag().toString());

        Log.e("onClick",images.get(position).image);

        activity.startActivity(new Intent(activity, Gallery.class).putExtra("url",images.get(position).image));


    }
}
