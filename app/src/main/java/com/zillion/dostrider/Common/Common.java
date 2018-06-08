package com.zillion.dostrider.Common;

import com.zillion.dostrider.Remote.FCMClient;
import com.zillion.dostrider.Remote.IFCMService;

public class Common {

    public static final String driver_tbl = "Drivers";
    public static final String user_driver_tbl = "Users";
    public static final String user_rider_tbl = "Riders";
    public static final String pickup_req_tbl = "PickupRequest";
    public static final String token_tbl = "Tokens";

    public static final String fcmURL = "https://fcm.googleapis.com/";

    public static IFCMService getFCMService() {

        return FCMClient.getClient(fcmURL).create(IFCMService.class);
    }

}
