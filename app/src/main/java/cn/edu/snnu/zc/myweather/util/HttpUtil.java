package cn.edu.snnu.zc.myweather.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class HttpUtil {

    public static void  sendOkHttpRequest(String address,okhttp3.Callback callback){
//     创建一个OkHttpClient的实例
        OkHttpClient client = new OkHttpClient();
//     创建一个Request对象,并设置要访问的目标网络地址
        Request request = new Request.Builder().url(address).build();
//        传入request对象，调用newCall方法
        client.newCall(request).enqueue(callback);
    }
}
