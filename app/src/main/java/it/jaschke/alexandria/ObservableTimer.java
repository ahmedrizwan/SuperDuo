package it.jaschke.alexandria;

import java.util.Timer;
import java.util.TimerTask;

import rx.Observable;
import rx.Subscriber;

public class ObservableTimer implements Observable.OnSubscribe<Void> {
    private int mSeconds;
    Timer mTimer;

    public ObservableTimer(int intervalSecond) {
        mSeconds = intervalSecond;
        mTimer = new Timer();
    }


    @Override
    public void call(final Subscriber<? super Void> subscriber) {
        //delay 1000ms, repeat in 5000ms
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                subscriber.onNext(null);
            }
        }, mSeconds, mSeconds);
    }
}