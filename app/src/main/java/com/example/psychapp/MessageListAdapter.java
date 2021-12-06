package com.example.psychapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.psychapp.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

class MessageListAdapter extends RecyclerView.Adapter {
    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;

    private Context mContext;
    private List<JSONObject> mMessageList;

    public MessageListAdapter(Context context, List<JSONObject> messageList) {
        mContext = context;
        mMessageList = messageList;
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    // Determines the appropriate ViewType according to the sender of the message.
    @Override
    public int getItemViewType(int position) {
        JSONObject message = (JSONObject) mMessageList.get(position);
        SharedPreferences sharedPref = mContext.getSharedPreferences(mContext.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        String userId = sharedPref.getString(mContext.getString(R.string.user_id), String.valueOf(R.string.user_def_id));
        String username="";
        try {
            username = message.getString("username");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (userId.equals(username)) {
            // If the current user is the sender of the message
            return VIEW_TYPE_MESSAGE_SENT;
        } else {
            // If some other user sent the message
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }
    }

    // Inflates the appropriate layout according to the ViewType.
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;

        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_me, parent, false);
            return new SentMessageHolder(view);
        } else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_other, parent, false);
            return new ReceivedMessageHolder(view);
        }

        return null;
    }

    void addMessage(JSONObject message) {
        mMessageList.add(message);
        notifyDataSetChanged();
    }
    // Passes the message object to a ViewHolder so that the contents can be bound to UI.
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        JSONObject message = (JSONObject) mMessageList.get(position);
        switch (holder.getItemViewType()) {
            case VIEW_TYPE_MESSAGE_SENT:
                try {
                    ((SentMessageHolder) holder).bind(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED:
                try {
                    ((ReceivedMessageHolder) holder).bind(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
        }
    }

    private class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText;

        SentMessageHolder(View itemView) {
            super(itemView);
            messageText = (TextView) itemView.findViewById(R.id.text_gchat_message_me);
            timeText = (TextView) itemView.findViewById(R.id.text_gchat_timestamp_me);
        }

        void bind(JSONObject message) throws JSONException {
            messageText.setText(message.getString("message"));

            // Format the stored timestamp into a readable String using method.
            timeText.setText(message.getString("created"));
        }
    }

    private class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText, nameText;
//        ImageView profileImage;

        ReceivedMessageHolder(View itemView) {
            super(itemView);

            messageText = (TextView) itemView.findViewById(R.id.text_gchat_message_other);
            timeText = (TextView) itemView.findViewById(R.id.text_gchat_timestamp_other);
            nameText = (TextView) itemView.findViewById(R.id.text_gchat_user_other);
//            profileImage = (ImageView) itemView.findViewById(R.id.image_message_profile);
        }

        void bind(JSONObject message) throws JSONException {
            messageText.setText(message.getString("message"));

            // Format the stored timestamp into a readable String using method.
            timeText.setText(message.getString("created"));

            nameText.setText(message.getString("displayName"));

            // Insert the profile image from the URL into the ImageView.
//            Utils.displayRoundImageFromUrl(mContext, message.getSender().getProfileUrl(), profileImage);
        }
    }
}