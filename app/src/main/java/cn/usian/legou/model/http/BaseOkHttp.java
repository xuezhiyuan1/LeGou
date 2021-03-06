package cn.usian.legou.model.http;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import cn.usian.legou.model.http.callback.ResultCallBack;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by TuLing on 2017/3/10.
 * 基本的网络请求
 * 发送get请求
 * 发送post请求
 */
public class BaseOkHttp<T> {

    /**
     * 抽取单例的OKHttpClient对象
     * 第一步 构造函数私有化
     * 第二步 提供一个共有的、静态的方法 该方法的返回值BaseOkHttp
     * 第三步 提供私有的静态的BaseOkHttp的对象
     */

    //保证OkHttpClient对象是单例的
    private OkHttpClient okHttpClient;

    private static BaseOkHttp baseOkHttp = null;
    private BaseOkHttp(){
        okHttpClient = new OkHttpClient.Builder().build();
    }

    public synchronized static BaseOkHttp getInstance(){
        if(baseOkHttp == null)
            baseOkHttp = new BaseOkHttp();
        return baseOkHttp;
    }


    /**
     * 发送get请求
     * @param url 请求地址
     * @param params 参数列表
     * @param callBack 请求的回调
     */
    public void get(String url, Map<String,String> params, final ResultCallBack<String> callBack){
        if(params != null && params.size() > 0) {
            StringBuffer sb = new StringBuffer(url);
            sb.append("?");
            Set<String> keys = params.keySet();

            for (String key : keys) {
                sb.append(key).append("=").append(params.get(key)).append("&");
            }

            url = sb.toString().substring(0, sb.length() - 1);
        }

        Request request = new Request.Builder().url(url).build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callBack.onError(e.getMessage().toString(),"404");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                callBack.onSuccess(response.body().string());
            }
        });

    }

    /**
     * 发送POST请求
     * @param url 请求地址
     * @param params 参数列表
     * @param callBack 请求的回调
     */
    public void post(String url, Map<String,String> params, final ResultCallBack<T> callBack){

        FormBody.Builder builder = null;
        if(params != null && params.size() > 0) {
            builder = new FormBody.Builder();

            Set<String> keys = params.keySet();

            for (String key : keys) {
                builder.add(key,params.get(key));
            }

        }

        Request request = new Request.Builder().url(url).post(builder.build()).build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callBack.onError(e.getMessage().toString(),"404");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                Gson gson = new Gson();
                T obj = gson.fromJson(result,new TypeToken<T>(){}.getType());
                callBack.onSuccess(obj);
            }
        });

    }

}
