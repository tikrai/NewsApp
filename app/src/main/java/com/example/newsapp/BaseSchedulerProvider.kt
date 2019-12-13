package com.example.newsapp

import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

interface BaseSchedulerProvider {
    fun ui(): Scheduler
    fun io(): Scheduler

    class SchedulerProvider : BaseSchedulerProvider {
        override fun ui() = AndroidSchedulers.mainThread()
        override fun io() = Schedulers.io()
    }

    class TrampolineSchedulerProvider : BaseSchedulerProvider {
        override fun ui() = Schedulers.trampoline()
        override fun io() = Schedulers.trampoline()
    }
}
