package in.codeomega.parents.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.recyclerview.widget.RecyclerView;

import in.codeomega.parents.R;
import in.codeomega.parents.model.Feedback;

public class FeedbackAdapter extends RecyclerView.Adapter<FeedbackAdapter.MyViewHolder> {

    ArrayList<Feedback> feedbacks;
    Context context;

    public FeedbackAdapter(Context context, ArrayList<Feedback> feedbacks) {

        this.context = context;
        this.feedbacks = feedbacks;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // infalte the item Layout
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_feedback, parent, false);

        // set the view's size, margins, paddings and layout parameters
        MyViewHolder vh = new MyViewHolder(v); // pass the view to View Holder
        return vh;
    }


    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

        Log.e("onBindViewHolder", position + "");

        holder.title.setText(feedbacks.get(position).title);
        holder.subtext.setText(feedbacks.get(position).feedback);
        holder.date.setText(feedbacks.get(position).date);
    }

    @Override
    public int getItemCount() {

        return feedbacks.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        // init the item view's

        private TextView title;
        private TextView subtext;
        private TextView date;


        public MyViewHolder(View itemView) {
            super(itemView);

            this.date = (TextView) itemView.findViewById(R.id.date);
            this.subtext = (TextView) itemView.findViewById(R.id.subtext);
            this.title = (TextView) itemView.findViewById(R.id.title);

        }
    }
}