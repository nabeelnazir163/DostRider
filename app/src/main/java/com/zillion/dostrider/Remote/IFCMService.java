package com.zillion.dostrider.Remote;

import com.zillion.dostrider.Model.FCMResponse;
import com.zillion.dostrider.Model.Sender;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAOQST74U:APA91bGaWlShOtn3WkyC_Khfz3aZNblwecbCwXuqFfU68nxQNCsVePnarAj7_Fh4MMG0Ie-kbbDNHuIoRXHImU3KJjJ6MLzva2CS_Tqg8XHNLztgY25ncRbD6eSQ4JsRndgLEk8RIDrL"
    })

    @POST("fcm/send")
    Call<FCMResponse> sendMessage(@Body Sender body);
}

