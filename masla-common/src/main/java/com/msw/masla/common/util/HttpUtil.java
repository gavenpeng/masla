package com.msw.masla.common.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.msw.masla.common.constant.Constants;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by gaoyue on 17/7/25.
 */
@Slf4j
public class HttpUtil {

    public static String sendGetReturnString(String url, String parameter) throws Exception {
        if (StringUtils.isBlank(url)) {
            return "";
        } else {
            if (!StringUtils.isBlank(parameter)) {
                url = url + "?" + parameter;
            }

            HttpClient httpClient = new HttpClient();
            httpClient.getHttpConnectionManager().getParams().setSoTimeout(2000);
            GetMethod method = new GetMethod(url);
            try {
                httpClient.executeMethod(method);
                String result = method.getResponseBodyAsString();
                return result;
            } catch (Exception var5) {
                method.releaseConnection();
                throw var5;
            }finally {
                method.releaseConnection();
                ((SimpleHttpConnectionManager)httpClient.getHttpConnectionManager()).shutdown();
            }

        }
    }




    public static String postGetReturnString(String url, Map<String, String> params) throws Exception {
        if (StringUtils.isBlank(url)) {
            return "";
        } else {

            HttpClient httpClient = new HttpClient();
            httpClient.getHttpConnectionManager().getParams().setSoTimeout(1000);
            PostMethod method = new PostMethod(url);
            for (Map.Entry<String, String> entry : params.entrySet()) {
                method.addParameter(entry.getKey(), entry.getValue());
            }
            try {
                httpClient.executeMethod(method);
                String result = method.getResponseBodyAsString();
                return result;
            } catch (Exception var5) {
                method.releaseConnection();
                throw var5;
            }finally {
                method.releaseConnection();
                ((SimpleHttpConnectionManager)httpClient.getHttpConnectionManager()).shutdown();
            }
        }
    }

    public static String postWithHeadersGetReturnString(String url, Map<String, String> params,
        Map<String, String> headers) throws Exception {
        if (StringUtils.isBlank(url)) {
            return "";
        } else {

            HttpClient httpClient = new HttpClient();
            httpClient.getHttpConnectionManager().getParams().setSoTimeout(1000);
            PostMethod method = new PostMethod(url);
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                method.setRequestHeader(entry.getKey(), entry.getValue());
            }
            for (Map.Entry<String, String> entry : params.entrySet()) {
                method.addParameter(entry.getKey(), entry.getValue());
            }
            try {
                httpClient.executeMethod(method);
                String result = method.getResponseBodyAsString();
                return result;
            } catch (Exception var5) {
                method.releaseConnection();
                throw var5;
            }finally {
                method.releaseConnection();
                ((SimpleHttpConnectionManager)httpClient.getHttpConnectionManager()).shutdown();
            }
        }
    }

    public static int sendGetReturnStatusCode(String url, String parameter) throws Exception {
        if (StringUtils.isBlank(url)) {
            return HttpStatus.SC_BAD_REQUEST;
        } else {
            if (!StringUtils.isBlank(parameter)) {
                url = url + "?" + parameter;
            }
            HttpClient httpClient = new HttpClient();
            httpClient.getHttpConnectionManager().getParams().setSoTimeout(3000);
            httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(3000);
            GetMethod method = new GetMethod(url);
            try {
                int responseCode =  httpClient.executeMethod(method);
                return responseCode;
            } catch (Exception var5) {
                method.releaseConnection();
                throw var5;
            }finally {
                method.releaseConnection();
                ((SimpleHttpConnectionManager)httpClient.getHttpConnectionManager()).shutdown();
            }
        }
    }

    public static String convertMapToParametersSuffix(Map<String, String> paramMap)
        throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : paramMap.entrySet()) {
            sb.append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue(), "UTF-8")).append("&");
        }
        String sign = SignUtils.sign(paramMap);
        sb.append("sign").append("=").append(sign);
        return sb.toString();
    }

    public static void main(String[] args) throws Exception {
//        Map<String, String> params = new HashMap<String, String>();
//        params.put(MonitorQueryDataType.POST_PARAM_APP_NAME, MonitorQueryDataType.APP_NAME);
//        params.put(MonitorQueryDataType.POST_PARAM_OPERATOR, MonitorQueryDataType.REFRESH_APP_CACHE);
//
//        //tring s = sendGetReturnString("http://localhost:20102/monitor", params);
//        String s = postGetReturnString("http://localhost:20102/monitor", params);
//        System.out.println(s);
//        JSONObject jsonObject = JSONObject.parseObject(s);
//        JSONObject dataJson = (JSONObject) jsonObject.get("data");
//        JSONArray appJson = (JSONArray) dataJson.get("apps");
//        for (int i = 0; i < appJson.size(); i++) {
//            System.out.println(appJson.get(i));
//        }

    }

    public static String doPost(String url,String query){
        String response = null;
        OutputStream out = null;
        InputStream in = null;
        try {
            URL restURL = new URL(url);
            /*
             * 此处的urlConnection对象实际上是根据URL的请求协议(此处是http)生成的URLConnection类 的子类HttpURLConnection
             */
            HttpURLConnection conn = (HttpURLConnection) restURL.openConnection();
            //请求方式
            conn.setRequestMethod("POST");
            //设置是否从httpUrlConnection读入，默认情况下是true; httpUrlConnection.setDoInput(true);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            out = conn.getOutputStream();
            out.write(query.getBytes("utf-8"));
            in = conn.getInputStream();
            byte[] data = new byte[512];
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int size = 0;
            while ((size=in.read(data)) >0){
                baos.write(data,0,size);
            }
            byte[] content = baos.toByteArray();
            response = new String(content, "utf-8");
        }catch(Throwable e){
            log.error("post to Anomaly Detection failed.");
        }finally{
            try {
                if(out!=null)
                    out.close();
            }catch(Throwable e){
                log.error("HttpURLConnection OutputStream close failed:",e);
            }try{
                if(in!=null)
                    in.close();
            }catch(Throwable e){
                log.error("HttpURLConnection InputStream close failed:",e);
            }
        }
        return response;
    }
}
