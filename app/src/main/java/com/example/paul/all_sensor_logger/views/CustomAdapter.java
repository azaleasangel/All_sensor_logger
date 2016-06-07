package com.example.paul.all_sensor_logger.views;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.paul.all_sensor_logger.R;

/**
 * A custom adapter for our listview
 * <p/>
 * If you check http://developer.android.com/reference/android/widget/Adapter.html you'll notice
 * there are several types. BaseAdapter is a good generic adapter that should suit all your needs.
 * Just implement all what's abstract and add your collection of data
 * <p/>
 * Created by hanscappelle on 7/10/14.
 * https://github.com/hanscappelle/so-2250770
 */
public class CustomAdapter extends BaseAdapter {
	private final static String TAG = "CustomAdapter";
    /**
     * this is our own collection of data, can be anything we want it to be as long as we get the
     * abstract methods implemented using this data and work on this data (see getter) you should
     * be fine
     */
    private List<ModelObject> mData;

    /**
     * some context can be useful for getting colors and other resources for layout
     */
    private Context mContext;

    /**
     * our ctor for this adapter, we'll accept all the things we need here
     *
     * @param mData
     */
    public CustomAdapter(final Context context, final List<ModelObject> mData) {
        this.mData = mData;
        this.mContext = context;
    }

    public List<ModelObject> getData() {
        return mData;
    }

    @Override
    public int getCount() {
        return mData != null ? mData.size() : 0;
    }

    @Override
    public Object getItem(int i) {
        return mData != null ? mData.get(i) : null;
    }

    @Override
    public long getItemId(int i) {
        // just returning position as id here, could be the id of your model object instead
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // this is where we'll be creating our view, anything that needs to update according to
        // your model object will need a view to visualize the state of that propery
    	View view = convertView;
    	
    	
        // the viewholder pattern for performance
        ViewHolder viewHolder = new ViewHolder();
        if (view == null) {

            // inflate the layout, see how we can use this context reference?
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            view = inflater.inflate(R.layout.list_item, parent, false);
            Log.d(TAG, String.format("Get view %d", position));
            // we'll set up the ViewHolder
            
            viewHolder.name = (TextView) view.findViewById(R.id.tv_name);
            viewHolder.addr = (TextView) view.findViewById(R.id.tv_addr);
            viewHolder.pair = (TextView) view.findViewById(R.id.tv_pair);
            viewHolder.sampling = (TextView) view.findViewById(R.id.tv_sampling);
            

            // store the holder with the view.
            view.setTag(viewHolder);

        } else {
            // we've just avoided calling findViewById() on resource every time
            // just use the viewHolder instead
            viewHolder = (ViewHolder) view.getTag();
        }

        // object item based on the position
        ModelObject obj = mData.get(position);

        // assign values if the object is not null
        if (mData != null) {
            // get the TextView from the ViewHolder and then set the text (item name) and other values
            viewHolder.name.setText(obj.getName());
            viewHolder.addr.setText(obj.getAddress());
            viewHolder.pair.setText(obj.getPair());
            viewHolder.sampling.setText(obj.getSampling());
        }
        return view;
    }

    private static class ViewHolder {
        public TextView name;
        public TextView addr;
        public TextView pair;
        public TextView sampling;
    }
}