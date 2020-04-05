package com.example.a2hands.chat.chat_notifications;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAygrkIck:APA91bHT8BWrHCBB2gMrf4LPX2gF5NDRqopFoxrEUqWCmv8RfCGK48dBEStZprdBWGIh2zyZgoqX8HeSeKicEgHTKO6CFYnVmXIzpoUdbJ7fotvoNNk5pTLBNM4t42Tgjtk0UYLDZbe3"
    })

    @POST("fcm/send")
    Call<Response> sendNotification(@Body Sender body);
}
