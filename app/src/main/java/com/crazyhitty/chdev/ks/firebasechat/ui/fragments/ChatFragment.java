package com.crazyhitty.chdev.ks.firebasechat.ui.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.crazyhitty.chdev.ks.firebasechat.R;
import com.crazyhitty.chdev.ks.firebasechat.core.chat.ChatContract;
import com.crazyhitty.chdev.ks.firebasechat.core.chat.ChatPresenter;
import com.crazyhitty.chdev.ks.firebasechat.events.PushNotificationEvent;
import com.crazyhitty.chdev.ks.firebasechat.models.Chat;
import com.crazyhitty.chdev.ks.firebasechat.ui.activities.TakePhotoDelegateActivity;
import com.crazyhitty.chdev.ks.firebasechat.ui.adapters.ChatRecyclerAdapter;
import com.crazyhitty.chdev.ks.firebasechat.utils.Constants;
import com.crazyhitty.chdev.ks.firebasechat.utils.UploadImageUtil;
import com.google.firebase.auth.FirebaseAuth;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.util.ArrayList;
import java.util.logging.MemoryHandler;

/**
 * Author: Kartik Sharma
 * Created on: 8/28/2016 , 10:36 AM
 * Project: FirebaseChat
 */

public class ChatFragment extends Fragment implements ChatContract.View, TextView.OnEditorActionListener {
    static final int REQ_TAKE_PHOTO = 0;

    private RecyclerView mRecyclerViewChat;
    private EditText mETxtMessage;
    private ImageView myAvatarIV;
    private ImageView otherAvatarIV;
    private Button uploadPicBtn;

    private ProgressDialog mProgressDialog;

    private ChatRecyclerAdapter mChatRecyclerAdapter;

    private ChatPresenter mChatPresenter;

    private String mChatId;
    private Handler mHandler;
    private Runnable mLoadingImageTask = new Runnable() {
        @Override
        public void run() {
            if (mOtherImageUrl == null) {
                return;
            }
            Glide.with(getActivity())
                    .load(mOtherImageUrl)
                    .priority(Priority.IMMEDIATE)
                    .skipMemoryCache(false)
                    .crossFade()
                    .dontTransform()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(otherAvatarIV);
        }
    };
    private String mMyImageUrl;
    private String mOtherImageUrl;
    private Runnable mLoadMeImageTask = new Runnable() {
        @Override
        public void run() {
            if (mMyImageUrl == null) {
                return;
            }
            Glide.with(getActivity())
                    .load(mMyImageUrl)
                    .priority(Priority.IMMEDIATE)
                    .skipMemoryCache(false)
                    .crossFade()
                    .dontTransform()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(myAvatarIV);
        }
    };

    public static ChatFragment newInstance(String receiver,
                                           String receiverUid,
                                           String firebaseToken) {
        Bundle args = new Bundle();
        args.putString(Constants.ARG_RECEIVER, receiver);
        args.putString(Constants.ARG_RECEIVER_UID, receiverUid);
        args.putString(Constants.ARG_FIREBASE_TOKEN, firebaseToken);
        ChatFragment fragment = new ChatFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_chat, container, false);
        bindViews(fragmentView);

        mHandler = new Handler();

        return fragmentView;
    }

    private void bindViews(View view) {
        mRecyclerViewChat = (RecyclerView) view.findViewById(R.id.recycler_view_chat);
        mETxtMessage = (EditText) view.findViewById(R.id.edit_text_message);
        myAvatarIV = (ImageView) view.findViewById(R.id.myAvatarIV);
        otherAvatarIV = (ImageView) view.findViewById(R.id.otherAvatarIV);
        uploadPicBtn = (Button) view.findViewById(R.id.uploadPicBtn);
        uploadPicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //FIXME: hardcode chatId
                final String receiverUid = getArguments().getString(Constants.ARG_RECEIVER_UID);
                final String senderUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                if(ChatRecyclerAdapter.VIEW_TYPE_ME == mChatRecyclerAdapter.getItemViewType(0)){
                    mChatId = senderUid + "_" + receiverUid;
                }else{
                    mChatId = receiverUid + "_" + senderUid;
                }

                startActivityForResult( new Intent(getActivity(), TakePhotoDelegateActivity.class), REQ_TAKE_PHOTO);
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();
    }

    private void init() {
        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setTitle(getString(R.string.loading));
        mProgressDialog.setMessage(getString(R.string.please_wait));
        mProgressDialog.setIndeterminate(true);

        mETxtMessage.setOnEditorActionListener(this);

        mChatPresenter = new ChatPresenter(this);
        mChatPresenter.getMessage(FirebaseAuth.getInstance().getCurrentUser().getUid(),
                getArguments().getString(Constants.ARG_RECEIVER_UID));
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEND) {
            sendMessage();
            return true;
        }
        return false;
    }

    private void sendMessage() {
        String message = mETxtMessage.getText().toString();

        if(!"".equals(message.trim())){
            String receiver = getArguments().getString(Constants.ARG_RECEIVER);
            String receiverUid = getArguments().getString(Constants.ARG_RECEIVER_UID);
            String sender = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            String senderUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            String receiverFirebaseToken = getArguments().getString(Constants.ARG_FIREBASE_TOKEN);
            Chat chat = new Chat(sender,
                    receiver,
                    senderUid,
                    receiverUid,
                    message,
                    System.currentTimeMillis()
            );
            mChatPresenter.sendMessage(getActivity().getApplicationContext(),
                    chat,
                    receiverFirebaseToken);
        }
    }

    @Override
    public void onSendMessageSuccess() {
        mETxtMessage.setText("");
        Toast.makeText(getActivity(), "Message sent", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSendMessageFailure(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onGetMessagesSuccess(Chat chat) {
        if (mChatRecyclerAdapter == null) {
            mChatRecyclerAdapter = new ChatRecyclerAdapter(new ArrayList<Chat>());
            mRecyclerViewChat.setAdapter(mChatRecyclerAdapter);
        }

        String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mChatRecyclerAdapter.add(chat);
        mRecyclerViewChat.smoothScrollToPosition(mChatRecyclerAdapter.getItemCount() - 1);
    }

    @Override
    public void onGetMessagesFailure(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    //FIXME: !!!about Avatar!!!
    @Override
    public void onSendAvatarSuccess() {

    }

    @Override
    public void onSendAvatarFailure(String message) {

    }

    @Override
    public void onGetAvatarSuccess(String userUid, String imageUrl) {
        String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (userUid.equals(myUid)) {
            mMyImageUrl = imageUrl;
            mHandler.removeCallbacks(mLoadMeImageTask);
            mHandler.postDelayed(mLoadMeImageTask, 500);
        } else {
            mOtherImageUrl = imageUrl;
            mHandler.removeCallbacks(mLoadingImageTask);
            mHandler.postDelayed(mLoadingImageTask, 500);
        }
    }

    @Override
    public void onGetAvatarFailure(String message) {

    }

    @Subscribe
    public void onPushNotificationEvent(PushNotificationEvent pushNotificationEvent) {
        if (mChatRecyclerAdapter == null || mChatRecyclerAdapter.getItemCount() == 0) {
            mChatPresenter.getMessage(FirebaseAuth.getInstance().getCurrentUser().getUid(),
                    pushNotificationEvent.getUid());
        }
    }

    @Override
    public void onActivityResult(int requestCode,
                                 int resultCode,
                                 final Intent data) {
        switch (requestCode) {
            case REQ_TAKE_PHOTO:
                if(data != null) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {

                            String senderUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                            final File file = new File(data.getData().getPath());
                            try {
                                //UploadImageUtil.uploadPhoto(file, "YoLung", "YoLung");
                                UploadImageUtil.upload(file, mChatId, senderUid);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            //
                        }
                    }).start();
                    break;
                }
            default:
                break;
        }
    }
}
