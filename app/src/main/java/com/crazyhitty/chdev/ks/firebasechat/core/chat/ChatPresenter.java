package com.crazyhitty.chdev.ks.firebasechat.core.chat;

import android.content.Context;

import com.crazyhitty.chdev.ks.firebasechat.models.Chat;

/**
 * Author: Kartik Sharma
 * Created on: 9/2/2016 , 10:05 PM
 * Project: FirebaseChat
 */

public class ChatPresenter implements ChatContract.Presenter,
        ChatContract.OnSendMessageListener,
        ChatContract.OnSendAvatarListener,
        ChatContract.OnGetMessagesListener,
        ChatContract.OnGetAvatarListener{
    private ChatContract.View mView;
    private ChatInteractor mChatInteractor;

    public ChatPresenter(ChatContract.View view) {
        this.mView = view;
        mChatInteractor = new ChatInteractor(this, this, this, this);
    }

    @Override
    public void sendMessage(Context context, Chat chat, String receiverFirebaseToken) {
        mChatInteractor.sendMessageToFirebaseUser(context, chat, receiverFirebaseToken);
    }

    @Override
    public void getMessage(String senderUid, String receiverUid) {
        mChatInteractor.getMessageFromFirebaseUser(senderUid, receiverUid);
    }

    @Override
    public void sendAvatar(Context context, Chat chat, String receiverFirebaseToken) {
//        mChatInteractor.sendMessageToFirebaseUser(ChatInteractor.TYPE_AVATAR, context, chat, receiverFirebaseToken);
    }

    @Override
    public void getAvatar(String senderUid, String receiverUid) {
//        mChatInteractor.getMessageFromFirebaseUser(ChatInteractor.TYPE_AVATAR, senderUid, receiverUid);
    }

    @Override
    public void onSendMessageSuccess() {
        mView.onSendMessageSuccess();
    }

    @Override
    public void onSendMessageFailure(String message) {
        mView.onSendMessageFailure(message);
    }

    @Override
    public void onGetMessagesSuccess(Chat chat) {
        mView.onGetMessagesSuccess(chat);
    }

    @Override
    public void onGetMessagesFailure(String message) {
        mView.onGetMessagesFailure(message);
    }





    @Override
    public void onSendAvatarSuccess() {
        mView.onSendMessageSuccess();
    }

    @Override
    public void onSendAvatarFailure(String message) {
        mView.onSendMessageFailure(message);
    }

    @Override
    public void onGetAvatarSuccess(String userUid, String imageUrl) {
        mView.onGetAvatarSuccess(userUid, imageUrl);
    }

    @Override
    public void onGetAvatarFailure(String message) {
        mView.onGetAvatarFailure(message);
    }
}
