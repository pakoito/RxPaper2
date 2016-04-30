
package com.pacoworks.rxpaper.sample;

import android.app.Application;

import com.pacoworks.rxpaper.RxPaperBook;

public class RxPaperSampleApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        RxPaperBook.init(this);
    }
}
