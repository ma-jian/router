package com.mm.router

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import java.io.File


/**
 * Created by : m
 * Date : 2022/3/23
 * An invisible fragment to embedded into activity for handling ActivityResultLauncher requests.
 * @since 1.1
 */

class RouterFragment : Fragment() {

    private lateinit var resultCallback: ActivityResultCallback<ActivityResult>
    private lateinit var uriCallback: ActivityResultCallback<Uri?>
    private lateinit var listUriCallback: ActivityResultCallback<List<Uri>>
    private lateinit var bitmapCallback: ActivityResultCallback<Bitmap?>
    private lateinit var booleanCallback: ActivityResultCallback<Boolean>
    private lateinit var mapBooleanCallback: ActivityResultCallback<Map<String, Boolean>>

    /**
     * launcher of StartActivityForResult
     */
    private val activityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (checkResultForGC()) {
            resultCallback.onActivityResult(it)
        }
    }

    /**
     * launcher of content:// media
     */
    private val contentLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) {
        if (checkUriForGC()) {
            uriCallback.onActivityResult(it)
        }
    }

    /**
     * launcher of content:// media
     */
    private val multipleContentsLauncher = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) {
        if (checkListUriForGC()) {
            listUriCallback.onActivityResult(it)
        }
    }

    /**
     * launcher of TakePicturePreview
     * [MediaStore.ACTION_IMAGE_CAPTURE]  take small a picture preview, returning it as a Bitmap
     */
    private val picPreviewLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) {
        if (checkBitmapForGC()) {
            bitmapCallback.onActivityResult(it)
        }
    }

    /**
     * launcher of TakePicture
     * [MediaStore.ACTION_IMAGE_CAPTURE] take a picture saving it into the provided content-[Uri].
     */
    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) {
        if (checkBooleanForGC()) {
            booleanCallback.onActivityResult(it)
        }
    }

    /**
     * launcher of TakeVideo
     * [MediaStore.ACTION_VIDEO_CAPTURE] take a video saving it into the provided content-[Uri].
     */
    private val takeVideoLauncher = registerForActivityResult(ActivityResultContracts.TakeVideo()) {
        if (checkBitmapForGC()) {
            bitmapCallback.onActivityResult(it)
        }
    }

    /**
     * launcher of PickContact
     * an to request the user to pick a contact from the contacts app.
     */
    private val pickContactLauncher = registerForActivityResult(ActivityResultContracts.PickContact()) {
        if (checkUriForGC()) {
            uriCallback.onActivityResult(it)
        }
    }


    /**
     * launcher of RequestMultiplePermissions
     * requestPermissions
     */
    private val permissionsLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        if (checkMapForGC()) {
            mapBooleanCallback.onActivityResult(it)
        }
    }

    /**
     * launcher of SendEmail
     */
    private val sendEmailLauncher = registerForActivityResult(ResultContracts.SendEmail()) {
        if (checkResultForGC()) {
            resultCallback.onActivityResult(it)
        }
    }

    /**
     * launcher of open map
     */
    private val mapLauncher = registerForActivityResult(ResultContracts.MapIntent()) {
        if (checkResultForGC()) {
            resultCallback.onActivityResult(it)
        }
    }

    /**
     * launcher of open call
     */
    private val callLauncher = registerForActivityResult(ResultContracts.CallIntent()) {
        if (checkResultForGC()) {
            resultCallback.onActivityResult(it)
        }
    }

    /**
     * launcher of send Sms
     */
    private val sendSmsLauncher = registerForActivityResult(ResultContracts.SendSMSIntent()) {
        if (checkResultForGC()) {
            resultCallback.onActivityResult(it)
        }
    }

    /**
     * launcher of send share
     */
    private val shareLauncher = registerForActivityResult(ResultContracts.SendShareIntent()) {
        if (checkResultForGC()) {
            resultCallback.onActivityResult(it)
        }
    }

    /**
     * launcher of open market
     */
    private val marketLauncher = registerForActivityResult(ResultContracts.MarketIntent()) {
        if (checkResultForGC()) {
            resultCallback.onActivityResult(it)
        }
    }

    /**
     * launcher of open system settings
     */
    private val settingsLauncher = registerForActivityResult(ResultContracts.SettingsIntent()) {
        if (checkResultForGC()) {
            resultCallback.onActivityResult(it)
        }
    }

    /**
     * 执行路由跳转到指定页面并返回结果
     * execute the route, jump to the specified page and return the result
     * @param callback the activity result callback
     */
    fun navigation(intent: Intent, callback: ActivityResultCallback<ActivityResult>): Boolean {
        this.resultCallback = callback
        return checkIntent(intent) {
            activityLauncher.launch(intent)
        }
    }

    /**
     * 选择相册内容
     * open content receiving a {@code content://} {@link Uri} for that content that allows you to use
     * [android.content.ContentResolver.openInputStream(Uri)] to access the raw data.
     * @param input The input is the mime type to filter by, e.g."image/\*"
     * @param callback content
     */
    fun navigationContent(input: Intent, callback: ActivityResultCallback<Uri?>) {
        this.uriCallback = callback
        contentLauncher.launch(input.getStringExtra(ResultContracts.Contents.TYPE))
    }

    /**
     * 多选
     * open content receiving a {@code content://} {@link Uri} for that content that allows you to use
     * [android.content.ContentResolver.openInputStream(Uri)] to access the raw data.
     * @param input The input is the mime type to filter by, e.g."image/\*"
     * @param callback content list
     */
    fun navigationMultipleContent(input: Intent, callback: ActivityResultCallback<List<Uri>>) {
        this.listUriCallback = callback
        multipleContentsLauncher.launch(input.getStringExtra(ResultContracts.Contents.TYPE) ?: "image/*")
    }

    /**
     * 打开相机拍照预览并返回bitmap
     * open [MediaStore.ACTION_IMAGE_CAPTURE] to take small a picture preview, returning it as a [Bitmap].
     * 跳转 TakePicturePreview need permission android.permission.CAMERA
     * @param callback returning a Bitmap
     */
    fun navigationTakePicPreview(callback: ActivityResultCallback<Bitmap?>) {
        checkPermission(Manifest.permission.CAMERA) {
            val permission = it[Manifest.permission.CAMERA] ?: false
            if (permission) {
                this.bitmapCallback = callback
                picPreviewLauncher.launch(null)
            } else {
                Router.LogE("RouterFragment: Permission [android.permission.CAMERA] :$permission")
                callback.onActivityResult(null)
            }
        }
    }

    /**
     * 打开相机拍照预览并保存
     * open to [MediaStore.ACTION_IMAGE_CAPTURE] take a picture saving it into the provided content-[Uri].
     * 跳转 TakePicture
     * @param intent  saving it into the provided content
     * @param callback  Returns true if the image was saved into the given [Uri].
     */
    fun navigationTakePicture(intent: Intent, callback: ActivityResultCallback<Boolean>) {
        checkPermission(Manifest.permission.CAMERA) {
            val permission = it[Manifest.permission.CAMERA] ?: false
            if (permission) {
                this.booleanCallback = callback
                val pic = intent.getStringExtra(ResultContracts.Contents.PATH)
                    ?: "${requireContext().cacheDir}/picture/${System.currentTimeMillis()}.jpg"
                val fileUri = fileUri(pic)
                takePictureLauncher.launch(fileUri)
            } else {
                Router.LogE("RouterFragment: Permission [android.permission.CAMERA] :$permission")
                callback.onActivityResult(false)
            }
        }
    }

    /**
     * 拍摄视频并保存返回预览图
     * open to [MediaStore.ACTION_VIDEO_CAPTURE] take a video saving it into the provided content-[Uri].
     * 跳转 TakePicture
     * @param intent  saving it into the provided content
     * @param callback  Returns a thumbnail.
     */
    fun navigationTakeVideo(intent: Intent, callback: ActivityResultCallback<Bitmap?>) {
        checkPermission(Manifest.permission.CAMERA) {
            val permission = it[Manifest.permission.CAMERA] ?: false
            if (permission) {
                this.bitmapCallback = callback
                val video = intent.getStringExtra(ResultContracts.Contents.PATH)
                    ?: "${requireContext().cacheDir}/video/${System.currentTimeMillis()}.mp4"
                val fileUri = fileUri(video)
                takeVideoLauncher.launch(fileUri)
            } else {
                Router.LogE("RouterFragment: Permission [android.permission.CAMERA] :$permission")
                callback.onActivityResult(null)
            }
        }
    }


    /**
     * 打开获取联系人
     * open to request the user to pick a contact from the contacts app.
     * 跳转 PickContact
     * @param callback   The result is a {@code content:} [Uri].
     */
    fun navigationPickContact(callback: ActivityResultCallback<Uri?>) {
        checkPermission(Manifest.permission.READ_CONTACTS) {
            val permission = it[Manifest.permission.READ_CONTACTS] ?: false
            if (permission) {
                this.uriCallback = callback
                pickContactLauncher.launch(null)
            } else {
                Router.LogE("RouterFragment: Permission [android.permission.READ_CONTACTS] :$permission")
                callback.onActivityResult(null)
            }
        }
    }

    /**
     * 发送邮件
     * @param intent
     */
    fun sendEmail(intent: Intent, callback: ActivityResultCallback<ActivityResult>) {
        this.resultCallback = callback
        val subject = intent.getStringExtra(ResultContracts.SendEmail.EXTRA_SUBJECT) ?: ""
        val text = intent.getStringExtra(ResultContracts.SendEmail.EXTRA_TEXT) ?: ""
        val email = intent.getStringExtra(ResultContracts.SendEmail.EXTRA_EMAIL) ?: ""
        val cc = intent.getStringExtra(ResultContracts.SendEmail.EXTRA_CC) ?: ""
        val stream = intent.getStringExtra(ResultContracts.SendEmail.EXTRA_STREAM) ?: Uri.EMPTY.toString()
        sendEmailLauncher.launch(
            mapOf(
                ResultContracts.SendEmail.EXTRA_SUBJECT to subject,
                ResultContracts.SendEmail.EXTRA_TEXT to text,
                ResultContracts.SendEmail.EXTRA_EMAIL to email,
                ResultContracts.SendEmail.EXTRA_CC to cc,
                ResultContracts.SendEmail.EXTRA_STREAM to stream,
            )
        )
    }

    /**
     * 发送短信
     * @param intent
     */
    fun sendSms(intent: Intent, callback: ActivityResultCallback<ActivityResult>) {
        this.resultCallback = callback
        val phone = intent.getStringExtra(ResultContracts.SendSMSIntent.PHONE) ?: ""
        val message = intent.getStringExtra(ResultContracts.SendSMSIntent.MESSAGE) ?: ""
        sendSmsLauncher.launch(
            mapOf(
                ResultContracts.SendSMSIntent.PHONE to phone, ResultContracts.SendSMSIntent.MESSAGE to message
            )
        )
    }

    /**
     * 系统分享
     * @param intent
     */
    fun sendShare(intent: Intent, callback: ActivityResultCallback<ActivityResult>) {
        this.resultCallback = callback
        val stream = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(ResultContracts.SendShareIntent.EXTRA_STREAM, Uri::class.java)
        } else intent.getParcelableExtra<Uri>(ResultContracts.SendShareIntent.EXTRA_STREAM)
        val text = intent.getStringExtra(ResultContracts.SendShareIntent.EXTRA_TEXT) ?: ""
        shareLauncher.launch(
            mapOf(
                ResultContracts.SendShareIntent.EXTRA_STREAM to (stream ?: Uri.EMPTY),
                ResultContracts.SendShareIntent.EXTRA_TEXT to text
            )
        )
    }

    /**
     * 打开地图
     * @param intent
     */
    fun openMap(intent: Intent, callback: ActivityResultCallback<ActivityResult>) {
        this.resultCallback = callback
        val mode = intent.getStringExtra(ResultContracts.MapIntent.MAP_MODE) ?: ""
        val dname = intent.getStringExtra(ResultContracts.MapIntent.DES_NAME) ?: ""
        mapLauncher.launch(mapOf(ResultContracts.MapIntent.MAP_MODE to mode, ResultContracts.MapIntent.DES_NAME to dname))
    }

    /**
     * 打开电话
     * @param intent
     */
    fun openCall(intent: Intent, callback: ActivityResultCallback<ActivityResult>) {
        this.resultCallback = callback
        callLauncher.launch(intent.getStringExtra(ResultContracts.CallIntent.PHONE))
    }

    /**
     * 打开应用市场
     * @param intent
     */
    fun openMarket(intent: Intent, callback: ActivityResultCallback<ActivityResult>) {
        this.resultCallback = callback
        marketLauncher.launch(intent.getStringExtra(ResultContracts.MarketIntent.PACKAGE_NAME))
    }

    /**
     * 打开系统设置
     * @param intent
     */
    fun openSettings(intent: Intent, callback: ActivityResultCallback<ActivityResult>): Boolean {
        return checkIntent(intent) {
            this.resultCallback = callback
            settingsLauncher.launch(intent.getStringExtra(ResultContracts.SettingsIntent.SETTINGS_ACTION))
        }
    }


    /**
     * check intent is safe
     * @param intent
     */
    private fun checkIntent(intent: Intent, block: () -> Unit): Boolean {
        return if (requireActivity().packageManager.resolveActivity(intent, 0) != null) {
            block.invoke()
            true
        } else false
    }

    /**
     * @param fileName
     */
    private fun fileUri(fileName: String): Uri {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) FileProvider.getUriForFile(
            requireContext(), requireContext().packageName + ".fileProvider", mkdirs(fileName)
        )
        else Uri.fromFile(mkdirs(fileName))
    }

    /**
     * check Permissions
     * @param permission Permissions to check
     */
    private inline fun checkPermission(vararg permission: String, crossinline block: (Map<String, Boolean>) -> Unit) {
        this.mapBooleanCallback = ActivityResultCallback<Map<String, Boolean>> {
            block.invoke(it)
        }
        permissionsLauncher.launch(arrayOf(*permission))
    }

    /**
     * make file
     * @param path
     */
    private fun mkdirs(path: String): File {
        val file = File(path)
        file.parentFile?.let {
            if (!it.exists()) {
                it.mkdirs()
            }
        }
        return file
    }

    private fun checkResultForGC(): Boolean {
        if (!::resultCallback.isInitialized) {
            logE("resultCallback")
            return false
        }
        return true
    }

    private fun checkUriForGC(): Boolean {
        if (!::uriCallback.isInitialized) {
            logE("uriCallback")
            return false
        }
        return true
    }

    private fun checkListUriForGC(): Boolean {
        if (!::listUriCallback.isInitialized) {
            logE("listUriCallback")
            return false
        }
        return true
    }

    private fun checkBitmapForGC(): Boolean {
        if (!::bitmapCallback.isInitialized) {
            logE("bitmapCallback")
            return false
        }
        return true
    }

    private fun checkBooleanForGC(): Boolean {
        if (!::booleanCallback.isInitialized) {
            logE("booleanCallback")
            return false
        }
        return true
    }

    private fun checkMapForGC(): Boolean {
        if (!::mapBooleanCallback.isInitialized) {
            logE("mapBooleanCallback")
            return false
        }
        return true
    }

    private fun logE(str: String) {
        Router.LogE("$str should not be null at this time, so we can do nothing in this case.")
    }
}