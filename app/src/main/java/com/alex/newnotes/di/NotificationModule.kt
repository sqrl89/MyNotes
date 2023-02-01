package com.alex.newnotes.di

import com.alex.newnotes.utils.notifications.NotificationUtils
import com.alex.newnotes.utils.notifications.NotificationUtilsImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationModule {

    @Binds
    abstract fun bindNotification(notificationUtils: NotificationUtilsImpl): NotificationUtils

}