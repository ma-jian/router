package com.mm.router

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources.NotFoundException
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
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
        if (checkResultForGC("StartActivityForResult")) {
            resultCallback.onActivityResult(it)
        }
    }

    /**
     * launcher of content:// media
     */
    private val contentLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) {
        if (checkUriForGC("GetContent")) {
            uriCallback.onActivityResult(it)
        }
    }

    /**
     * launcher of content:// media
     */
    private val multipleContentsLauncher = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) {
        if (checkListUriForGC("GetMultipleContents")) {
            listUriCallback.onActivityResult(it)
        }
    }

    /**
     * launcher of TakePicturePreview
     * [MediaStore.ACTION_IMAGE_CAPTURE]  take small a picture preview, returning it as a Bitmap
     */
    private val picPreviewLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) {
        if (checkBitmapForGC("TakePicturePreview")) {
            bitmapCallback.onActivityResult(it)
        }
    }

    /**
     * launcher of TakePicture
     * [MediaStore.ACTION_IMAGE_CAPTURE] take a picture saving it into the provided content-[Uri].
     */
    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) {
        if (checkBooleanForGC("TakePicture")) {
            booleanCallback.onActivityResult(it)
        }
    }

    /**
     * launcher of TakeVideo
     * [MediaStore.ACTION_VIDEO_CAPTURE] take a video saving it into the provided content-[Uri].
     */
    private val takeVideoLauncher = registerForActivityResult(ActivityResultContracts.CaptureVideo()) {
        if (checkBooleanForGC("CaptureVideo")) {
            booleanCallback.onActivityResult(it)
        }
    }

    /**
     * launcher of PickContact
     * an to request the user to pick a contact from the contacts app.
     */
    private val pickContactLauncher = registerForActivityResult(ActivityResultContracts.PickContact()) {
        if (checkUriForGC("PickContact")) {
            uriCallback.onActivityResult(it)
        }
    }


    /**
     * launcher of RequestMultiplePermissions
     * requestPermissions
     */
    private val permissionsLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        if (checkMapForGC("RequestMultiplePermissions")) {
            mapBooleanCallback.onActivityResult(it)
        }
    }

    /**
     * launcher of open map
     */
    private val mapLauncher = registerForActivityResult(ResultContracts.MapIntent()) {
        if (checkResultForGC("MapIntent")) {
            resultCallback.onActivityResult(it)
        }
    }

    /**
     * launcher of open call
     */
    private val callLauncher = registerForActivityResult(ResultContracts.CallIntent()) {
        if (checkResultForGC("CallIntent")) {
            resultCallback.onActivityResult(it)
        }
    }

    /**
     * launcher of send Sms
     */
    private val sendSmsLauncher = registerForActivityResult(ResultContracts.SendSMSIntent()) {
        if (checkResultForGC("SendSMSIntent")) {
            resultCallback.onActivityResult(it)
        }
    }

    /**
     * launcher of send share
     */
    private val shareLauncher = registerForActivityResult(ResultContracts.SendShareIntent()) {
        if (checkResultForGC("SendShareIntent")) {
            resultCallback.onActivityResult(it)
        }
    }

    /**
     * launcher of open market
     */
    private val marketLauncher = registerForActivityResult(ResultContracts.MarketIntent()) {
        if (checkResultForGC("MarketIntent")) {
            resultCallback.onActivityResult(it)
        }
    }

    /**
     * launcher of open system settings
     */
    private val settingsLauncher = registerForActivityResult(ResultContracts.SettingsIntent()) {
        if (checkResultForGC("SettingsIntent")) {
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
    fun navigationContent(input: Intent, callback: ActivityResultCallback<ActivityResult>) {
        this.uriCallback = ActivityResultCallback<Uri?> { uri ->
            val intent1 = Intent().apply {
                data = uri
            }
            val activityResult =
                ActivityResult(if (uri != null) Activity.RESULT_OK else Activity.RESULT_CANCELED, intent1)
            callback.onActivityResult(activityResult)
        }
        contentLauncher.launch(input.getStringExtra(ResultContracts.Contents.TYPE) ?: "image/*")
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
                Router.LogE("RouterFragment: no Permission [android.permission.CAMERA]")
                callback.onActivityResult(null)
            }
        }
    }

    /**
     * 打开相机拍照预览并保存
     * open to [MediaStore.ACTION_IMAGE_CAPTURE] take a picture saving it into the provided content-[Uri].
     * 跳转 TakePicture
     * @param intent  saving it into the provided content
     * @param callback  Returns path if the image was saved into the given [Uri].
     */
    fun navigationTakePicture(intent: Intent, callback: ActivityResultCallback<ActivityResult>) {
        checkPermission(Manifest.permission.CAMERA) {
            val permission = it[Manifest.permission.CAMERA] ?: false
            if (permission) {
                val pic = intent.getStringExtra(ResultContracts.Contents.PATH)
                    ?: (requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.absolutePath + "/take_picture.jpg")
                val fileUri = fileUri(pic)
                this.booleanCallback = ActivityResultCallback<Boolean> { bol ->
                    val intent1 = Intent().apply {
                        data = fileUri
                    }
                    val activityResult =
                        ActivityResult(if (bol) Activity.RESULT_OK else Activity.RESULT_CANCELED, intent1)
                    callback.onActivityResult(activityResult)
                }
                takePictureLauncher.launch(fileUri)
            } else {
                Router.LogE("RouterFragment: no Permission [android.permission.CAMERA]")
                callback.onActivityResult(ActivityResult(Activity.RESULT_CANCELED, Intent()))
            }
        }
    }

    /**
     * 拍摄视频并保存返回视频地址
     * open to [MediaStore.ACTION_VIDEO_CAPTURE] take a video saving it into the provided content-[Uri].
     * 跳转 TakePicture
     * @param intent  saving it into the provided content
     * @param callback  Returns a path of video.
     */
    fun navigationTakeVideo(intent: Intent, callback: ActivityResultCallback<ActivityResult>) {
        checkPermission(Manifest.permission.CAMERA) {
            val permission = it[Manifest.permission.CAMERA] ?: false
            if (permission) {
                val video = intent.getStringExtra(ResultContracts.Contents.PATH)
                    ?: (requireContext().getExternalFilesDir(Environment.DIRECTORY_MOVIES)?.absolutePath + "/take_video.mp4")
                val fileUri = fileUri(video)
                this.booleanCallback = ActivityResultCallback<Boolean> { bol ->
                    val intent1 = Intent().apply {
                        data = fileUri
                    }
                    val activityResult = ActivityResult(if (bol) Activity.RESULT_OK else Activity.RESULT_CANCELED, intent1)
                    callback.onActivityResult(activityResult)
                }
                takeVideoLauncher.launch(fileUri)
            } else {
                Router.LogE("RouterFragment: no Permission [android.permission.CAMERA]")
                callback.onActivityResult(ActivityResult(Activity.RESULT_CANCELED, Intent()))
            }
        }
    }


    /**
     * 打开获取联系人
     * open to request the user to pick a contact from the contacts app.
     * 跳转 PickContact
     * @param callback   The result is a {@code content:} [Uri].
     */
    fun navigationPickContact(callback: ActivityResultCallback<ActivityResult>) {
        checkPermission(Manifest.permission.READ_CONTACTS) {
            val permission = it[Manifest.permission.READ_CONTACTS] ?: false
            if (permission) {
                this.uriCallback = ActivityResultCallback<Uri?> { uri ->
                    val intent1 = Intent().apply {
                        data = uri
                    }
                    val activityResult =
                        ActivityResult(if (uri != null) Activity.RESULT_OK else Activity.RESULT_CANCELED, intent1)
                    callback.onActivityResult(activityResult)
                }
                pickContactLauncher.launch(null)
            } else {
                Router.LogE("RouterFragment: no Permission [android.permission.READ_CONTACTS]")
                callback.onActivityResult(ActivityResult(Activity.RESULT_CANCELED, Intent()))
            }
        }
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
        return try {
            this.resultCallback = callback
            settingsLauncher.launch(intent.getStringExtra(ResultContracts.SettingsIntent.SETTINGS_ACTION))
            true
        } catch (ex: Exception) {
            ex.printStackTrace()
            false
        }
    }


    /**
     * check intent is safe
     * @param intent
     */
    private fun checkIntent(intent: Intent, block: () -> Unit): Boolean {
        return if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                requireActivity().packageManager.resolveActivity(
                    intent,
                    PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong())
                ) != null
            else requireActivity().packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null
        ) {
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

    private fun checkResultForGC(str: String): Boolean {
        if (!::resultCallback.isInitialized) {
            logE("$str resultCallback")
            return false
        }
        return true
    }

    private fun checkUriForGC(str: String): Boolean {
        if (!::uriCallback.isInitialized) {
            logE("$str uriCallback")
            return false
        }
        return true
    }

    private fun checkListUriForGC(str: String): Boolean {
        if (!::listUriCallback.isInitialized) {
            logE("$str listUriCallback")
            return false
        }
        return true
    }

    private fun checkBitmapForGC(str: String): Boolean {
        if (!::bitmapCallback.isInitialized) {
            logE("$str bitmapCallback")
            return false
        }
        return true
    }

    private fun checkBooleanForGC(str: String): Boolean {
        if (!::booleanCallback.isInitialized) {
            logE("$str booleanCallback")
            return false
        }
        return true
    }

    private fun checkMapForGC(str: String): Boolean {
        if (!::mapBooleanCallback.isInitialized) {
            logE("$str mapBooleanCallback")
            return false
        }
        return true
    }

    private fun logE(str: String) {
        Router.LogE("$str should not be null at this time, so we can do nothing in this case.")
    }
}