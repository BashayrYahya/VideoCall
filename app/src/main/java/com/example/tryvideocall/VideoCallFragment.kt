package com.example.tryvideocall

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.agora.rtc.IRtcEngineEventHandler
import io.agora.rtc.RtcEngine
import io.agora.rtc.video.VideoCanvas

private const val PERMISSION_REQ_ID_RECORD_AUDIO = 22
private const val PERMISSION_REQ_ID_CAMERA = PERMISSION_REQ_ID_RECORD_AUDIO + 1

class VideoCallFragment : Fragment() {


    // Fill the App ID of your project generated on Agora Console.
    private val APP_ID = "abcbccc386c6473c9455f897b2bd2555"
    // Fill the channel name.
    private val CHANNEL = "somyahChannel"
    // Fill the temp token generated on Agora Console.
    private val TOKEN = "006abcbccc386c6473c9455f897b2bd2555IACU1+srnKc9K7hGmjvnDMP4migJhWDe+y4BZHJHGFVPFJCWrnsAAAAAEACVNqlcyuLKYQEAAQDK4sph"


    private lateinit var mRtcEngine: RtcEngine


    private val mRtcEventHandler = object : IRtcEngineEventHandler() {
        // Listen for the remote user joining the channel to get the uid of the user.
        override fun onUserJoined(uid: Int, elapsed: Int) {
            activity?.runOnUiThread {
                // Call setupRemoteVideo to set the remote video view after getting uid from the onUserJoined callback.
                setupRemoteVideo(uid)
            }
        }
    }


    private fun checkSelfPermission(permission: String, requestCode: Int): Boolean {
        if (ContextCompat.checkSelfPermission(requireContext(), permission) !=
            PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                arrayOf(permission),
                requestCode)
            return false
        }
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO, PERMISSION_REQ_ID_RECORD_AUDIO)
            && checkSelfPermission(Manifest.permission.CAMERA, PERMISSION_REQ_ID_CAMERA)) {
            initializeAndJoinChannel()
        }
    }
    private fun initializeAndJoinChannel() {
        try {
            mRtcEngine = RtcEngine.create(activity, APP_ID, mRtcEventHandler)
        } catch (e: Exception) {
        }
        // By default, video is disabled, and you need to call enableVideo to start a video stream.
        mRtcEngine.enableVideo()

        val localContainer =view?.findViewById<FrameLayout>(R.id.local_video_view_container)
        // Call CreateRendererView to create a SurfaceView object and add it as a child to the FrameLayout.
        val localFrame = RtcEngine.CreateRendererView(activity)
        localContainer?.addView(localFrame)
        // Pass the SurfaceView object to Agora so that it renders the local video.
        mRtcEngine.setupLocalVideo(VideoCanvas(localFrame, VideoCanvas.RENDER_MODE_FIT, 0))

        // Join the channel with a token.
        mRtcEngine.joinChannel(TOKEN, CHANNEL, "", 0)
    }


    // Kotlin

    private fun setupRemoteVideo(uid: Int) {
        val remoteContainer = view?.findViewById<FrameLayout>(R.id.remote_video_view_container)

        val remoteFrame = RtcEngine.CreateRendererView(activity)
        remoteFrame.setZOrderMediaOverlay(true)
        remoteContainer?.addView(remoteFrame)
        mRtcEngine.setupRemoteVideo(VideoCanvas(remoteFrame, VideoCanvas.RENDER_MODE_FIT, uid))

    }

    override fun onDestroy() {
        super.onDestroy()

        mRtcEngine.leaveChannel()
        RtcEngine.destroy()
    }


    // اضافتي

    private val mRtcEngineEventHandler = object : IRtcEngineEventHandler() {
            override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
                super.onJoinChannelSuccess(channel, uid, elapsed)
                Log.d("rohit", "user joind channel: $uid")
                requireActivity().runOnUiThread {
                    setupRemoteVideo(uid)
                }
            }

            override fun onUserOffline(uid: Int, reason: Int) {
                super.onUserOffline(uid, reason)
            }

            override fun onFirstRemoteVideoFrame(uid: Int, width: Int, height: Int, elapsed: Int) {
                super.onFirstRemoteVideoFrame(uid, width, height, elapsed)
                requireActivity().runOnUiThread {
                    setupRemoteVideo(uid)
                }
            }

            override fun onUserJoined(uid: Int, elapsed: Int) {
                super.onUserJoined(uid, elapsed)
                Log.d("rohit", "user joind: $uid")
                requireActivity().runOnUiThread {
                    setupRemoteVideo(uid)
                }
            }
        }
}



