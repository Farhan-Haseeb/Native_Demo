package com.example.example_android.util.download;

import java.util.concurrent.TimeUnit;

import okhttp3.ConnectionPool;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;

/**
 * HttpUtil wrapper, async connection pool mode
 */
public class HttpUtil {
    /**
     * Http connect timeout
     */
    private static final int minCONNECT_TIMEOUT = 3000;
    /**
     * Http write timeout
     */
    private static final int minWRITE_TIMEOUT = 3000;
    /**
     * Http Read timeout
     */
    private static final int minREAD_TIMEOUT = 3000;
    /**
     * Http Async Call Timeout
     */
    private static final int minCall_TIMEOUT = 3000;
    /**
     * Http connection pool
     */
    private static final int connectionPoolSize = 1000;
//    /**
//     * Static Http request pool
//     */
//    private static OkHttpClient client=null;
    /**
     * Static connection pool object
     */
    private static ConnectionPool mConnectionPool = new ConnectionPool(connectionPoolSize, 30, TimeUnit.MINUTES);
    //executor = new ThreadPoolExecutor(0, 2147483647, ...this one is static, 2.1 billion connections
//    private static RealConnectionPool mConnectionPool=new RealConnectionPool(connectionPoolSize, 30, TimeUnit.MINUTES);
    /**
     * ContentType
     */
    private static final String ContentType = "application/json;charset=utf-8";
    /**
     * AcceptType
     */
    private static final String AcceptType = "application/json;charset=utf-8";
    /**
     * Content-Type
     */
    private static final MediaType MediaType_ContentType = MediaType.parse(ContentType);

    /**
     * Get the Http Client object
     * Reduce connection time
     *
     * @return
     */
    public static OkHttpClient getHttpClient() {
        OkHttpClient client = new OkHttpClient.Builder()
//                .addInterceptor(new RetryIntercepter(3))//Retry 3 times
//                .addInterceptor(new GzipRequestInterceptor())//gzip compression
                .connectTimeout(minCONNECT_TIMEOUT, TimeUnit.MILLISECONDS) //Connect timeout
                .readTimeout(minREAD_TIMEOUT, TimeUnit.MILLISECONDS) //Read timeout
                .writeTimeout(minWRITE_TIMEOUT, TimeUnit.MILLISECONDS) //Write timeout
                // okhttp by default uses a RealConnectionPool whose initial thread count ==2147483647, which leaves lots of threads on the server side in TIMED_WAITING
                //ThreadPoolExecutor(0, 2147483647, 60L, TimeUnit.SECONDS, new SynchronousQueue(), Util.threadFactory("OkHttp ConnectionPool", true));
                .connectionPool(mConnectionPool)
                .build();
        return client;
    }

}
