package com.crazyhitty.chdev.ks.firebasechat.utils;

import android.net.Uri;

import org.apache.http.NameValuePair;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.json.JSONException;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by luyolung on 07/04/2017.
 */

public class UploadImageUtil {

    private static final String PARAM_USER_PHOTO = "image";
    private static final String PARAM_CHAT_ROOM_ID = "chat_room_key";
    private static final String PARAM_USER_ID = "user_uid";

    private static String SERVER_URL = "http://emochat-server.herokuapp.com/api/messages/upload";

    public static void uploadPhoto(Uri photoPath, String chatRoomId, String uid) throws IOException, JSONException {
        File file = new File(photoPath.getPath());
        uploadPhoto(file, chatRoomId, uid);
    }

    public static void uploadPhoto(File file, String chatRoomId, String uid) throws IOException, JSONException {

        MultipartEntity entity = new MultipartEntity();

        ContentBody body = new FileBody(file, "binary/octet-stream");

        entity.addPart(PARAM_USER_PHOTO, body);
        entity.addPart(PARAM_CHAT_ROOM_ID, new StringBody(chatRoomId) );
        entity.addPart(PARAM_USER_ID, new StringBody(uid) );

        HttpURLConnection conn = (HttpURLConnection) (new URL(SERVER_URL)).openConnection();
        conn.setRequestMethod("POST");
        conn.addRequestProperty("Content-length", String.valueOf(entity.getContentLength()));
        conn.addRequestProperty(entity.getContentType().getName(), entity.getContentType().getValue());
        conn.setDoOutput(true);
        OutputStream os = new BufferedOutputStream(conn.getOutputStream());
        entity.writeTo(os);
        os.close();
        conn.connect();
    }

    public static void upload(File file, String chatRoomId, String uid) {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) (new URL(SERVER_URL)).openConnection();
            conn.setRequestMethod("POST");

            MultipartEntity entity = new MultipartEntity();
            ByteArrayBody bin = new ByteArrayBody(toBytes(new FileInputStream(file)), "image/jpeg", "emochat.jpeg");
            entity.addPart(PARAM_USER_PHOTO, bin);

            Charset charset = Charset.forName("utf-8");
            entity.addPart(PARAM_CHAT_ROOM_ID, new StringBody(chatRoomId, "text/plain", charset));
            entity.addPart(PARAM_USER_ID, new StringBody(uid, "text/plain", charset));


            conn.addRequestProperty("Content-length", String.valueOf(entity.getContentLength()));
            conn.addRequestProperty(entity.getContentType().getName(), entity.getContentType().getValue());
            conn.setDoOutput(true);
            OutputStream os = new BufferedOutputStream(conn.getOutputStream());
            entity.writeTo(os);
            os.close();
            conn.connect();
            int statusCode = conn.getResponseCode();
            InputStream is;
            try {
                is = conn.getInputStream();
            } catch (FileNotFoundException e) {
                is = conn.getErrorStream();
            }
            String content = new String(toBytes(is), "UTF-8");
            System.out.println("content : " + content);
        } catch (IOException e) {
            conn = null;
        }
    }

    public static byte[] toBytes(InputStream is){
        byte[] result = null;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            copy(is, baos);
            result = baos.toByteArray();
        } catch (IOException ignored){}

        try {
            if (is != null) {
                is.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }


    private static final int IO_BUFFER_SIZE = 1024 * 4;
    private static final long LOW_RAM_THREASHOLD = 512 * 1024 * 1024; // 500 MB

    public static void copy(InputStream in, OutputStream out) throws IOException {
        copy(in, out, 0);
    }

    public static void copy(InputStream in, OutputStream out, int max) throws IOException {
        byte[] b = new byte[IO_BUFFER_SIZE];
        int read;
        while((read = in.read(b)) != -1){
            out.write(b, 0, read);
        }
    }
}
