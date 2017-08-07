package de.mpg.mpdl.labcam.code.utils;

import android.content.Context;
import android.widget.Toast;


public class ToastUtils {

        public static boolean isShow = true;

        /**
         * Method show short Toast
         *
         * @param context
         * @param message
         */
        public static void showShortMessage(Context context, CharSequence message)
        {
            if (isShow)
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }

        /**
         * Method show short Toast
         *
         * @param context
         * @param message
         */
        public static void showShortMessage(Context context, int message)
        {
            if (isShow)
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }

        /**
         * Method show long Toast
         *
         * @param context
         * @param message
         */
        public static void showLongMessage(Context context, CharSequence message)
        {
            if (isShow)
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        }

        /**
         * Method show long Toast
         *
         * @param context
         * @param message
         */
        public static void showLongMessage(Context context, int message)
        {
            if (isShow)
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        }

        /**
         * Method show customised Toast
         *
         * @param context
         * @param message
         * @param duration
         */
        public static void showMessage(Context context, CharSequence message, int duration)
        {
            if (isShow)
                Toast.makeText(context, message, duration).show();
        }

        /**
         * Method show customised Toast
         *
         * @param context
         * @param message
         * @param duration
         */
        public static void showmessage(Context context, int message, int duration)
        {
            if (isShow)
                Toast.makeText(context, message, duration).show();
        }

}
