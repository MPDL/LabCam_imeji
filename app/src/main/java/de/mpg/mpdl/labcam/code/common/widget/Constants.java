/*
 * Copyright (c) 2016. Xiaomu Tech.(Beijing) LLC. All rights reserved.
 */

package de.mpg.mpdl.labcam.code.common.widget;

/**
 * Created by yingli on 10/19/15.
 */
public class Constants {

    // host ip
    public static final String HOST_IP = "localhost"; // test server

    public static final String MAIN_WEBVIEW_URL = "https://www.yituishui.com/Application/mainAdvertise";

    public static String HOST_PORT = "8080";

    public static final String URL_RULES = "https://www.yituishui.com/Application/rules";

    public static final String URL_PROCEDURE = "https://www.yituishui.com/Application/procedure";

    // name of shared preferences
    public static final String SHARED_PREFERENCES = "myPref";

    public static final String USER_ID = "userId";

    public static final String API_KEY = "apiKey";

    public static final String EMAIL = "email";

    public static final String IS_GUIDE = "isGuide";

    // for checking update(function not used so org not changed)
    public static final String UPDATE_URL = "http://safetytaxfree.org/upload/update.txt";

    public static final String APK_FILE_NAME = "yituishui.apk";

    // splash cover timeout
    public static final long SPLASH_TIME_OUT = 4000;

    public static final String PREFERENCE = "myPref";

    public static final String ALIAS = "alias";

    public static final String CURRENT_FORM_GROUP_ID = "currentFormGroup";

    // static pages
    public static final String URL_UPGRADE_BENEFITS = "https://www.yituishui.com/Application/upgradeBenefits";

    public static final String URL_INVITE = "https://www.yituishui.com/Application/invite";

    public static final String URL_TRACK = "https://www.yituishui.com/Application/track";

    public static final String URL_STEPS = "https://www.yituishui.com/Application/steps";

    public static final String URL_HELP = "https://www.yituishui.com/Application/taxfreeguide";

    public static final String URL_TERMS = "https://www.yituishui.com/application/terms";

    public static final String URL_SHOP = "https://www.yituishui.com/application/shop";

    public static final String URL_STOREFINDER = "https://www.google.com/maps/d/u/0/viewer?mid=1VEWUZK0r4WIVGBFMo1IORP-7I4k";

    public static final String URL_SHOP_INTRO = "https://www.yituishui.com/Application/purchaseManual";

//    public static final String URL_TEST = "https://www.google.com/maps/d/embed?mid=1VEWUZK0r4WIVGBFMo1IORP-7I4k";

    // Database (Hibernate)
    public static final String PERSISTENCE_UNIT_NAMEURL_TERMS = "tuishuibao";

    // Jersey
    public static final String JSON_MEDIA_TYPE_PRODUCE = "application/json; charset=utf-8";

    public static final String JSON_MEDIA_TYPE_CONSUME = "application/json";

    public static final String JSON_MEDIA_TYPE_FORM = "application/x-www-form-urlencoded";

    public static final String JSON_MULTIPART_FORM_DATA = "multipart/form-data";

    // location sorting search range
    // when sorting by location, set a square range of longitude and latitude for searching
    // this value is set by measuring on Baidu map API by manually picking 六环 in beijing
    // TODO known problem is that, longitude shrink when it approach south and north pole
    // So the range is not guaranteed to be perfect square
    public static final double SEARCH_RANGE = 0.304705;

    public static final int MAX_ITEM = 500;

    // For calculating distance
    public static final double EARTH_RADIUS = 6371.0; // kilometers (or 3958.75 miles)

    // Upload target

    //for allen development env
    public static final String UPLOAD_FOLDER = "/Users/allen/apache-tomcat-7.0.62/webapps/upload/";

    public static final String SERVER_UPLOAD_URL = "http://localhost:8080/upload/";

    //for jeff development env
//    	public static final String UPLOAD_FOLDER = "/Users/xiaojielin/Documents/workspace/eclipse/.metadata/.plugins/org.eclipse.wst.server.core/tmp0/wtpwebapps/linshi/upload/";
//    	public static String SERVER_UPLOAD_URL = "http://"+HOST_IP+":"+HOST_PORT+"/linshi/upload/";

    //for production env
//	public static final String UPLOAD_FOLDER = "/root/apache-tomcat-7.0.62/webapps/upload/";  // use this when deploy to test server
//	public static final String SERVER_UPLOAD_URL = "http://"+HOST_IP+":"+HOST_PORT+"/upload/"; // use this when deploy to test server

    public static final String PINGPP_APP_KEY = "sk_test_bzzX5SPaParPKaPOSKGSWLWT";

    public static final String GOOGLE_APP_KEY = "AIzaSyDg6rgPRPq2GQMScfgjq-J1sZP7CdL83SU";

    public static final String BAIDU_API = "http://api.map.baidu.com/geocoder/v2/";

    public static final String BAIDU_APP_KEY = "x89ASh10SaQ4NvPVZfXYTfG1";

    public static final String WeChat_APP_ID = "wx3a743b08347e8b6a";

    public static final String WeiBo_APP_ID = "1097486450";

    public static final String WeiBo_APP_KEY = "937571124";

    public static final String WeiBo_REDIRECT_URL = "https://api.weibo.com/oauth2/default.html";

    public static final String WeiBo_SCOPE = "email";

    public static final String CHINESE = "中文服务";

    public static final String ENGLISH = "English";


    public static final String MQ_ID = "4ddee0a5b581c01c3cec84292c66e690";

    public static final String KD_SF_URL = "http://www.kuaidi100.com/chaxun?com=shunfeng&nu=";

    public static final String KD_EMS_URL = "http://www.kuaidi100.com/chaxun?com=ems&nu=";

    public static final String KD_URL = "http://www.kuaidi100.com/chaxun?";//=shunfeng&nu=";

//    public static final String KD_URL_PHONE = "https://m.kuaidi100.com/index_all.html?type=shunfeng&postid=";

    public class PostInfo {
        public static final String POST_NAME = "post_name";

        public static final String POST_DETAL = "post_detail";

        public static final String POST_PHONE = "post_phone";

        public static final String POST_IDCARD = "post_idcard";

    }

    public static long REFRESH_TIME = 1 * 60 * 1000;


    public static String NEW_REST_SERVER = "https://www.safetytaxfree.de/rest/v1/";
    public static String NEW_DEV_SERVER = "https://www.safetytaxfree.org/rest/v1/";
//    public static String NEW_DEV_SERVER = "http://114.112.65.156:8080/rest/v1/";
//    public static String NEW_DEV_SERVER = "http://192.168.31.110:8080/tuishuibao/rest/v1/";

    public static final String STATUS_SUCCESS = "SUCCESS";

    public static final String KEY_CLASS_NAME = "key_class_name";

    public static final String IS_SHOW_UPDATE_DIALOG = "is_show_update_dialog";


    public static class WebView {
        public static String TITLE = "title";
        public static String URL = "url";

    }
}
