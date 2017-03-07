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

    public static String HOST_PORT = "8080";

    // splash cover timeout
    public static final long SPLASH_TIME_OUT = 4000;

    public static final String PREFERENCE = "myPref";

    public static final String ALIAS = "alias";

    public static final String CURRENT_FORM_GROUP_ID = "currentFormGroup";

    // Jersey
    public static final String JSON_MEDIA_TYPE_PRODUCE = "application/json; charset=utf-8";

    public static final String JSON_MEDIA_TYPE_CONSUME = "application/json";

    public static final String JSON_MEDIA_TYPE_FORM = "application/x-www-form-urlencoded";

    public static final String JSON_MULTIPART_FORM_DATA = "multipart/form-data";

    public static long REFRESH_TIME = 1 * 60 * 1000;

    public static String NEW_REST_SERVER = "https://www.safetytaxfree.de/rest/v1/";
    public static String NEW_DEV_SERVER = "https://www.safetytaxfree.org/rest/v1/";

    public static final String STATUS_SUCCESS = "SUCCESS";

    public static final String KEY_CLASS_NAME = "key_class_name";

    public static final String IS_SHOW_UPDATE_DIALOG = "is_show_update_dialog";


    public static class WebView {
        public static String TITLE = "title";
        public static String URL = "url";

    }

    /**************************   SHARED_PREFERENCES **********************************/
    public static final String SHARED_PREFERENCES = "myPref"; // name of shared preferences

    public static final String API_KEY = "apiKey";
    public static final String USER_ID = "userId";
    public static final String USER_NAME = "username";
    public static final String FAMILY_NAME = "familyName";
    public static final String GIVEN_NAME = "givenName";
    public static final String PASSWORD = "password";

    public static final String EMAIL = "email";
    public static final String SERVER_NAME = "serverName";
    public static final String OTHER_SERVER = "otherServer";
    public static final String COLLECTION_ID = "collectionID";
    public static final String OCR_IS_ON = "ocrIsOn";
    public static final String IS_ALBUM = "isAlbum";


}
