package com.example.gtj_sd_dg240.net;

import java.io.IOException;
import java.net.SocketTimeoutException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 重试拦截器
 */
public class RetryInterceptor implements Interceptor {

    private final int maxRetries;
    private final long retryDelayMs;

    public RetryInterceptor(int maxRetries, long retryDelayMs) {
        this.maxRetries = maxRetries;
        this.retryDelayMs = retryDelayMs;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        int retryCount = 0;
        IOException lastException = null;

        while (retryCount <= maxRetries) {
            try {
                return chain.proceed(request);
            } catch (SocketTimeoutException e) {
                e.printStackTrace();
                lastException = e;
                if (retryCount >= maxRetries) break;

                // 等待指数退避时间（2^retryCount * retryDelayMs）
                long waitTime = (long) Math.pow(2, retryCount) * retryDelayMs;
                try {
                    Thread.sleep(waitTime);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Retry interrupted", ie);
                }
                retryCount++;
            }
        }

        // 超过最大重试次数，抛出异常或返回错误响应
        throw new RetryFailedException("Max retries (" + maxRetries + ") exceeded", lastException);
    }

    // 自定义异常类型
    public static class RetryFailedException extends IOException {
        public RetryFailedException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
