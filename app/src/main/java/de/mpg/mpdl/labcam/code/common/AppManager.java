package de.mpg.mpdl.labcam.code.common;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;

import java.util.Stack;

/**
 * Application Activity management
 */
public class AppManager {

	private static Stack<Activity> activityStack;
	private static AppManager instance;

	private AppManager(){}
	/**
	 * single instance
	 */
	public static AppManager getAppManager(){
		if(instance==null){
			instance=new AppManager();
		}
		return instance;
	}
	/**
	 * add Activity to stack
	 */
	public void addActivity(Activity activity){
		if(activityStack==null){
			activityStack=new Stack<Activity>();
		}
		activityStack.add(activity);
	}
	/**
	 * get current Activity（last in stack）
	 */
	public Activity currentActivity(){
		Activity activity=activityStack.lastElement();
		return activity;
	}
	/**
	 * finish Activity（last in stack）
	 */
	public void finishActivity(){
		Activity activity=activityStack.lastElement();
		finishActivity(activity);
	}
	/**
	 * finish this Activity
	 */
	public void finishActivity(Activity activity){
		if(activity!=null){
			activityStack.remove(activity);
		}
	}
	/**
	 * finish activity with name
	 */
	public void finishActivity(Class<?> cls){
		for (Activity activity : activityStack) {
			if(activity.getClass().equals(cls) ){
				finishActivity(activity);
			}
		}
	}
	/**
	 * finish all Activity
	 */
	public void finishAllActivity(){
		if (activityStack != null) {
			for (int i = 0, size = activityStack.size(); i < size; i++){
				if (null != activityStack.get(i)){
					activityStack.get(i).finish();
				}
			}
			activityStack.clear();
		}
	}
	/**
	 * exit
	 */
	public void exitApp(Context context) {
		try {
			finishAllActivity();
			ActivityManager activityMgr= (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
			activityMgr.restartPackage(context.getPackageName());
			System.exit(0);
		} catch (Exception e) {
			Log.e("Activity Manager", "exiting app failed: " + e.toString());
		}
	}
}

