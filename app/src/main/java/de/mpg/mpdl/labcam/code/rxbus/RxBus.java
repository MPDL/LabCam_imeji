package de.mpg.mpdl.labcam.code.rxbus;

import android.util.Log;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

/**
 * Created by yingli on 2/21/17.
 */

public class RxBus {
    private static volatile RxBus defaultInstance;
    private final Subject<Object, Object> bus;
    private Subscription subscription;
    private Observable observable;
    private final Map<Class<?>, Object> mStickyEventMap;

    private RxBus(){
        bus = new SerializedSubject<>(PublishSubject.create());
        mStickyEventMap = new ConcurrentHashMap<>();
    }

    public static RxBus getDefault(){
        if (defaultInstance == null) {
            synchronized (RxBus.class) {
                if (defaultInstance == null) {
                    defaultInstance = new RxBus();
                }
            }
        }
        return defaultInstance;
    }

    public void post(Object event) {
        bus.onNext(event);
    }

    public <T> RxBus observe(Class<T> eventType) {
        observable = bus.ofType(eventType).subscribeOn(AndroidSchedulers.mainThread());
        return this;
    }

    public <T> RxBus observeSticky(final Class<T> eventType) {
        synchronized (mStickyEventMap) {
            observable = bus.ofType(eventType).subscribeOn(AndroidSchedulers.mainThread());
            final Object event = mStickyEventMap.get(eventType);

            if (event != null) {
                observable =  observable.mergeWith(Observable.create(new Observable.OnSubscribe<T>() {
                    @Override
                    public void call(Subscriber<? super T> subscriber) {
                        subscriber.onNext(eventType.cast(event));
                    }
                }));
            }
            return this;
        }

    }

    public RxBus threadMode(ThreadMode mode){
        if(null == observable) {
            throw new IllegalStateException("observable is null. you must call observe() first!");
        }
        synchronized (observable) {
            switch (mode){
                case POSTING:
                    observable = observable.observeOn(Schedulers.immediate());
                    break;
                case MAIN:
                    observable = observable.observeOn(AndroidSchedulers.mainThread());
                    break;
                case BACKGROUND:
                    observable = observable.observeOn(Schedulers.newThread());
                    break;
                case ASYNC:
                    observable = observable.observeOn(Schedulers.newThread());
                    break;
                default:
                    observable = observable.observeOn(Schedulers.immediate());
            }
        }
        return this;
    }

    public void postSticky(Event event){
        synchronized (mStickyEventMap) {
            mStickyEventMap.put(event.getClass(), event);
        }
        post(event);
    }

    public <T> Subscription subscribe(final EventSubscriber<T> subscriber){
        subscription = observable.subscribe(new Action1<T>() {
            @Override
            public void call(T o) {
                try{
                    subscriber.onEvent(o);
                } catch (Exception e){
                    e.printStackTrace();
                }

            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                //TODO handle error event
                Log.e("RxBus","an error occurred !");
            }
        });
        return subscription;
    }

    public Observable getObservable(){
        return observable;
    }

    public void removeStickyEvent(Class<?> eventType) {
        if(mStickyEventMap.containsKey(eventType)) {
            mStickyEventMap.remove(eventType);
        }
    }

    public void removeAllStickyEvent(){
        mStickyEventMap.clear();
    }
}
