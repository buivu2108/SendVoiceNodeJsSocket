package com.example.sendvoice

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sendvoice.databinding.ActivityMainBinding
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONException
import org.json.JSONObject
import java.io.*


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var myRecorder: MediaRecorder? = null
    private val myPlayer: MediaPlayer? = null
    private var outputFile: String? = null
    lateinit var mSocket: Socket
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
        initEvent()
        mSocket.on("server-send-voice", onNewMessage)
    }

    private fun initEvent() {
        binding.btnRecord.setOnClickListener {
            start(it)
        }
        binding.btnDone.setOnClickListener {
            stop(it)
        }
        binding.btnSend.setOnClickListener {
            outputFile = Environment.getExternalStorageDirectory().absolutePath + "/khoaphamvn.3gpp";
            val path: String = outputFile.toString()
            val voiceSend:ByteArray = fileLocalToByte(path)
            mSocket.emit("client-send-voice",voiceSend)
        }
    }

    private fun initView() {
        SocketHandler.setSocket()
        SocketHandler.connectionSocket()
        mSocket = SocketHandler.getSocket()
    }

    @SuppressLint("WrongConstant")
    private fun start(view: View?) {
        try {
            outputFile = Environment.getExternalStorageDirectory().absolutePath + "/khoaphamvn.3gpp"
            myRecorder = MediaRecorder()
            myRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
            myRecorder?.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            myRecorder?.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB)
            myRecorder?.setOutputFile(outputFile)
            myRecorder?.prepare()
            myRecorder?.start()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        Toast.makeText(applicationContext, "Start recording...",
            Toast.LENGTH_SHORT).show()
    }

    private fun stop(view: View?) {
        try {
            myRecorder?.stop()
            myRecorder?.release()
            myRecorder = null
            Toast.makeText(applicationContext, "Stop recording...",
                Toast.LENGTH_SHORT).show()
        } catch (e: java.lang.IllegalStateException) {
            e.printStackTrace()
        } catch (e: RuntimeException) {
            e.printStackTrace()
        }
    }

    private fun fileLocalToByte(path: String): ByteArray {
        val file = File(path)
        val size = file.length().toInt()
        val bytes = ByteArray(size)
        try {
            val buf = BufferedInputStream(FileInputStream(file))
            buf.read(bytes, 0, bytes.size)
            buf.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return bytes
    }
    private val onNewMessage = Emitter.Listener { args ->
        runOnUiThread(Runnable {
            val data = args[0] as JSONObject
            val voiceReceive: ByteArray
            try {
                voiceReceive = data.get("noidung") as ByteArray
                playMp3FromByte(voiceReceive)
            } catch (e: JSONException) {
                return@Runnable
            }
        })
    }
    private fun playMp3FromByte(mp3SoundByteArray: ByteArray) {
        try {
            val tempMp3 = File.createTempFile("kurchina", "mp3", cacheDir)
            tempMp3.deleteOnExit()
            val fos = FileOutputStream(tempMp3)
            fos.write(mp3SoundByteArray)
            fos.close()
            val mediaPlayer = MediaPlayer()
            val fis = FileInputStream(tempMp3)
            mediaPlayer.setDataSource(fis.fd)
            mediaPlayer.prepare()
            mediaPlayer.start()
        } catch (ex: IOException) {
            val s = ex.toString()
            ex.printStackTrace()
        }
    }
}