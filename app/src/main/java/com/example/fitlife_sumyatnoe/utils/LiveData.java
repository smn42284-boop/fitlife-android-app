package com.example.fitlife_sumyatnoe.utils;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class LiveData<T> {
    private T mData;
    private List<Observer<T>> mObservers = new ArrayList<>();

    public interface Observer<T> {
        void onChanged(@Nullable T data);
    }

    public LiveData(T initialValue) {
        mData = initialValue;
    }

    @MainThread
    public void observe(@NonNull Observer<T> observer) {
        if (!mObservers.contains(observer)) {
            mObservers.add(observer);
        }
        observer.onChanged(mData);
    }

    @MainThread
    public void removeObserver(@NonNull Observer<T> observer) {
        mObservers.remove(observer);
    }

    @MainThread
    protected void setValue(T value) {
        mData = value;
        for (Observer<T> observer : mObservers) {
            observer.onChanged(mData);
        }
    }

    @MainThread
    protected void postValue(T value) {
        // In Java, we'll use Handler for thread safety
        android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
        mainHandler.post(() -> setValue(value));
    }

    @Nullable
    public T getValue() {
        return mData;
    }
}