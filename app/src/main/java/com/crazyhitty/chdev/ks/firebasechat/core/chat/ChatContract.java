package com.crazyhitty.chdev.ks.firebasechat.core.chat;

import android.content.Context;

import com.crazyhitty.chdev.ks.firebasechat.models.Chat;

/**
 * Author: Kartik Sharma
 * Created on: 8/28/2016 , 11:06 AM
 * Project: FirebaseChat
 */

public interface ChatContract {
    interface View {
        void onSendMessageSuccess();

        void onSendMessageFailure(String message);

        void onGetMessagesSuccess(Chat chat);

        void onGetMessagesFailure(String message);

        void onSendAvatarSuccess();

        void onSendAvatarFailure(String message);


        void onGetAvatarSuccess(String userUid, String imageUrl);

        void onGetAvatarFailure(String message);
    }

    interface Presenter {
        void sendMessage(Context context, Chat chat, String receiverFirebaseToken);

        void getMessage(String senderUid, String receiverUid);

        void sendAvatar(Context context, Chat chat, String receiverFirebaseToken);

        void getAvatar(String userUid, String imageUrl);

        void removeListener();
    }

    interface Interactor {
        void sendMessageToFirebaseUser(Context context, Chat chat, String receiverFirebaseToken);

        void getMessageFromFirebaseUser(String senderUid, String receiverUid);

        void removeListener();
    }

    interface OnSendMessageListener {
        void onSendMessageSuccess();

        void onSendMessageFailure(String message);
    }

    interface OnGetMessagesListener {
        void onGetMessagesSuccess(Chat chat);

        void onGetMessagesFailure(String message);
    }

    interface OnSendAvatarListener{
        void onSendAvatarSuccess();

        void onSendAvatarFailure(String message);
    }

    interface OnGetAvatarListener{
        void onGetAvatarSuccess(String userUid, String imageUrl);

        void onGetAvatarFailure(String message);
    }
}
