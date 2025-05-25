/*
 * Copyright 2025 msw
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.msw.masla.common.util;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Map;

public class HttpClientUtil {
	private static Logger LOGGER = LoggerFactory
			.getLogger(HttpClientUtil.class);

	private static HttpClient httpClient;

	public static synchronized HttpClient getHttpClient(){
	  if(httpClient == null){
        httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());
      }
      return httpClient;
    }

	public static String postDoPostURL(String url, byte[] content) throws Throwable{
		HttpClient httpClient = getHttpClient();
		// 创建POST方法的 实例
		PostMethod postMethod = new PostMethod(url);
		postMethod.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, 1000);
//		postMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(0, false));
		postMethod.addRequestHeader("Content-Type","application/json;charset=UTF-8");

		postMethod.getParams().setParameter(
				HttpMethodParams.HTTP_CONTENT_CHARSET, "UTF-8");
		RequestEntity requestEntity = new ByteArrayRequestEntity(content);
		postMethod.setRequestEntity(requestEntity);

		return doPostDoPostURL(url, postMethod);
	}

	public static String postDoPostURL(String url, Map<String, Object> paramMap) throws Throwable{
		HttpClient httpClient = getHttpClient();
		// 创建POST方法的 实例
		PostMethod postMethod = new PostMethod(url);
		postMethod.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, 30000);
		postMethod.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "UTF-8");
		postMethod.addRequestHeader("Content-Type","application/json;charset=UTF-8");

		for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
			postMethod.setParameter(entry.getKey(), entry.getValue().toString());
		}

		return doPostDoPostURL(url, postMethod);
	}

	private static String doPostDoPostURL(String url, PostMethod postMethod) throws Throwable{
		String response = null;
		try {
			// 执行postMethod,并取得状态码
			int statusCode = httpClient.executeMethod(postMethod);
			InputStream is = postMethod.getResponseBodyAsStream();
			if(is != null) {
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				byte[] bytes = new byte[1024];
				int length = -1;
				while ((length = is.read(bytes)) > 0) {
					outputStream.write(bytes, 0, length);
				}
				byte[] result = outputStream.toByteArray();
				response = new String(result, "UTF-8");
				if (statusCode != 200) {
					response = null;
					LOGGER.error(url + "->Method failed: "
							+ postMethod.getStatusLine());
				}
				is.close();
			}


		} catch (Exception e) {
			//response = null;
			LOGGER.error(url + "->" + e.getMessage(), e);
			throw e;
		}finally {
			postMethod.releaseConnection();
		}
		return response;
	}


}
