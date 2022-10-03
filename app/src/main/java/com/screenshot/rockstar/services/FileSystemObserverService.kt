package com.screenshot.rockstar.services

import android.app.Service
import android.content.Intent
import android.os.FileObserver
import android.os.IBinder
import android.util.Log
import com.screenshot.rockstar.utils.Constants


private lateinit var serviceListener: FileSystemObserverService.DirectoryChangeService

class FileSystemObserverService: Service() {

    private var mFileObserver: FileObserver? = null
    private  var isFileWritten = false

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            Log.d("FileObserver", "Inside Intent")

            if((intent.hasExtra(Constants.INTENT_SERVICE_PATH))) {
                val intentPath = intent.getStringExtra(Constants.INTENT_SERVICE_PATH)
                mFileObserver =
                    object : FileObserver(intentPath) {
                        override fun onEvent(event: Int, path: String?) {
                            var newPath:String = path.toString()

                            if (path != null) {
                                if (path.startsWith(".pending")){
                                    newPath = "Screenshot" + path.split("Screenshot").last()
                                    Log.d("FileObserver", "starts with pending $newPath")

                                }
                            }
                            val filePath = intentPath + newPath

                            when (event) {
                                DELETE -> {
                                    Log.d("FileObserver", "File deleted $filePath")
                                    serviceListener.fileDelete(filePath)
                                }
                                CREATE -> {
                                    Log.d("FileObserver", "File created $filePath")
                                    serviceListener.fileCreate(filePath)
                                }
                                CLOSE_NOWRITE -> {
                                    //Log.d("FileObserver", "File close no write created $filePath")

                                }
                                CLOSE_WRITE -> {
                                    // Log.d("FileObserver", "File close write $filePath")
                                }
                                MODIFY -> {
                                    //Log.d("FileObserver", "File modify $filePath")
                                }
                                DELETE_SELF -> {
                                    //Log.d("FileObserver", "File delete self $filePath")
                                }
                                }


                        }

                    }
            }
        }
        mFileObserver?.startWatching() // The FileObserver starts watching
        return START_NOT_STICKY

    }

    interface DirectoryChangeService{
        fun fileDelete(filePath: String)
        fun fileCreate(filePath: String)

    }

    fun setServiceListener(listener: DirectoryChangeService){
        serviceListener = listener

    }


}