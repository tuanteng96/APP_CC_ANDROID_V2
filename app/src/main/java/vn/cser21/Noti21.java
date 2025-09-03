package vn.cser21;

import java.util.HashMap;
import java.util.Map;

public class Noti21 {


    public Notification21 notification;
    public String priority = "high";
    Map<String, String> data;

    Noti21() {
        notification = new Notification21();
        //notification.title = "";
        //notification.body = "";
        data = new HashMap<>();
    }

}
