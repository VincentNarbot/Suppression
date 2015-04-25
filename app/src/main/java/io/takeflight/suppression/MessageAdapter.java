package io.takeflight.suppression;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.ParseObject;

import java.util.List;

/**
 * Created by vincentnarbot on 4/24/15.
 */
public class MessageAdapter extends ArrayAdapter<ParseObject> {

    protected Context mContext;
    protected List<ParseObject> mMessages;

    public MessageAdapter(Context context, List<ParseObject> messages) {
        super(context, R.layout.message_item, messages);
        mContext = context;
        mMessages = messages;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        //Check if View already exist
        if(convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.message_item, null);
            holder = new ViewHolder();
            holder.messageIcon = (ImageView) convertView.findViewById(R.id.messageIcon);
            holder.messageLabel = (TextView) convertView.findViewById(R.id.messageLabel);
            convertView.setTag(holder);
        }
        else{
            holder = (ViewHolder)convertView.getTag();
        }

        ParseObject message = mMessages.get(position);

        if(message.getString(AppConstants.KEY_FILE_TYPE).equals(AppConstants.TYPE_IMAGE)){
            holder.messageIcon.setImageResource(R.drawable.ic_action_picture);
        }else{
            holder.messageIcon.setImageResource(R.drawable.ic_action_video);
        }
        holder.messageLabel.setText(message.getString(AppConstants.KEY_SENDER_NAME));

        return convertView;
    }

    private static class ViewHolder{
        ImageView messageIcon;
        TextView messageLabel;
    }
}
