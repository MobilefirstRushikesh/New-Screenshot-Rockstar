package com.screenshot.rockstar.repository

import com.screenshot.rockstar.database.AppDatabase
import javax.inject.Inject

class ImageDetailRepository @Inject constructor(
        apiHelper: AppDatabase
    ){
        private val dao = apiHelper.userDao()

    }
