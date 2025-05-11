package com.msw.masla.core;


import com.msw.masla.common.util.StringBuilderHolder;
import com.msw.masla.common.util.StringUtil;
import com.msw.masla.protocol.http.netty.context.SessionContext;

import java.util.regex.Matcher;

import com.msw.masla.protocol.http.netty.session.IOSession;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import org.apache.commons.lang.StringUtils;

import java.util.regex.Pattern;

/**
 * Created by Gavin.peng on 2024/2/9.
 */
public class ServiceIdFormatUtil {

    private static final String PATH_PREFIX = "$";
    private static final String PATH_TAIL = PATH_PREFIX + "tail";
    private static final String BLANK = "";
    private static final Pattern version_pattern = Pattern.compile("^v\\d+");
    private static final Pattern digital_pattern = Pattern.compile("\\d");
    private static final Pattern chinese_pattern = Pattern.compile("[\\u4e00-\\u9fa5]");
    private static final Pattern param_pattern = Pattern.compile("/\\$\\d+");
    private static final Pattern char_pattern = Pattern.compile(".*[a-zA-Z]+.*");


    public static boolean isUNvalidUrl(String url){
        if(url.endsWith(".htm")
                ||url.endsWith(".html")
                ||url.endsWith(".zip")
                ||url.endsWith(".tar")
                ||url.endsWith(".js")
                ||url.endsWith(".php")
                ||url.endsWith(".cfc")
                ||url.endsWith(".dll")
                ||url.endsWith(".exe")
                ||url.endsWith(".asp")
                ||url.endsWith(".gz")
                ||url.endsWith(".7z")
                ||url.endsWith(".txt")
                ||url.endsWith(".rar")){
            return true;
        }
        return false;

    }


    public static String formatServerId(String url, SessionContext<IOSession, HttpRequest, HttpResponse> requestContext){

        String realTarget = requestContext ==null?null:requestContext.getRewritePath();

        String regexIndex = PATH_PREFIX+0;
        if(!StringUtil.isEmptyString(realTarget) && realTarget.contains(regexIndex)) {
            realTarget =  StringUtils.replace(realTarget,regexIndex,requestContext.getSession().getContextRoot());
        }

        String[] urlParts = url.split("/");
        StringBuilder serverPath = StringBuilderHolder.getGlobal();
        int pathIndex = 1;
        int beginIndex = 0;
        for (int i = 0; i < urlParts.length; i++) {
            String part = urlParts[i];
            if(StringUtil.isEmptyString(part))
                continue;
            serverPath.append("/");
            part = part.trim();

            regexIndex = PATH_PREFIX+pathIndex;//$1
            if(!StringUtil.isEmptyString(realTarget) && realTarget.contains(regexIndex)) {
                realTarget =  StringUtils.replace(realTarget,regexIndex,part);
                // update to latest index for subString
                for (int j = 0; j <= i; j++) {
                    String tmp = urlParts[j];
                    if (StringUtil.isEmptyString(tmp)) {
                        continue;
                    }
                    beginIndex += (1 + tmp.length());
                }
            }

            if (isVersionTag(part)) {
                serverPath.append(part);
            } else if (containsDigit(part)){
                serverPath.append("{d}");
            }  else {
                if(isChar(part)){
                    serverPath.append(chinese_pattern.matcher(part).replaceAll(""));
                }else{
                    serverPath.append("{d}");
                }
            }
            pathIndex++;
        }

        if(realTarget != null) {
            if (realTarget.contains(PATH_TAIL)) {
                Matcher matcher = param_pattern.matcher(realTarget);
                if (matcher.find()) {
                    //如果$tail之前还有'/$n' 说明后端路径比前端路径短, $n用来标注$tail的开始位置
                    // e.g. 前端: /mobile/settings/(.*) ==> /mobile-settings/(.*)
                    //           要配置/mobile-settings/$2$tail, 必须有$2表示'settings'这段不要
                    //          (.*) 不一定存在，$2$tail找不到要替换的path段，直接去掉即可
                    realTarget = matcher.replaceAll(BLANK);
                    realTarget = StringUtils.replace(realTarget, PATH_TAIL, BLANK);
                } else {
                    // 0: path没有rewrite；
                    // >0: 追加rewrite后面的url；
                    String target = beginIndex == 0 ? url : url.substring(beginIndex);
                    realTarget = StringUtils.replace(realTarget, PATH_TAIL, target);
                }
            }
            requestContext.setRewritePath(realTarget);
        }

        return serverPath.toString();
    }

    private static boolean isVersionTag(String str) {
        return version_pattern.matcher(str).matches();
    }

    private static boolean isChar(String str) {
        return char_pattern.matcher(str).matches();
    }

    private static boolean containsDigit(String str) {
        return digital_pattern.matcher(str).find();
    }

    private static boolean isTimestamp(String str) {
        if (str.contains("ts") || str.contains("tm")) {
            return true;
        }
        return false;
    }


    public static void main(String[] arg){

//        String digitalString = "abc-12345-abc";
//        System.out.println(digital_pattern.matcher(digitalString).find());
        String redirectUrl = "/$0/v3/$2/$3";

//        String requestPath = "ximalayaos-wearkid-frontend-project/٢٦٧٢٦٣٥٨٠我";
        String requestPath = "ximalayaos-wearkid-frontend-project/sdfsdfs-我";
        String contextRoot = "";
        String path = "";
        System.out.println(formatServerId(requestPath,null));

        String regex=".*[a-zA-Z]+.*";

        Matcher m=Pattern.compile(regex).matcher("٢٦٧٢٦٣٥٨٠a我");
        if(m.matches()){
            System.out.println(formatServerId(requestPath,null));
        }else {
            System.out.println("false");
        }

    }
}

