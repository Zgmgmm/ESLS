import android.content.Context
import com.google.gson.Gson
import com.qmuiteam.qmui.widget.dialog.QMUIDialog
import dev.zgmgmm.esls.activity.LoginActivity
import dev.zgmgmm.esls.exception.RequestException
import dev.zgmgmm.esls.model.Response
import dev.zgmgmm.esls.showFailTipDialog
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.getStackTraceString
import org.jetbrains.anko.info
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class RequestExceptionHandler {
    companion object : AnkoLogger {
        override val loggerTag: String
            get() = "ESLS"

        fun handle(context: Context, throwable: Throwable) {
            info("Request error ${throwable.getStackTraceString()}")
            when (throwable) {
                is RequestException -> handRequestException(context, throwable)
                is HttpException -> handleHttpException(context, throwable)
                is IOException -> handleIOException(context, throwable)
                is Error -> handleError(context, throwable)
            }
        }

        private fun handRequestException(context: Context, exception: RequestException) {
            context.showFailTipDialog(exception.message, duration = 5000)
        }

        private fun handleError(context: Context, error: Error) {
            context.showFailTipDialog("未知错误: $error")
        }

        private fun handleIOException(context: Context, exception: IOException) {
            val msg = when (exception) {
                is SocketTimeoutException -> "网络超时"
                is UnknownHostException -> "未知域名，请检查服务器设置"
                is SocketException -> "连接失败，请检查网络"
                else -> "网络异常 $exception"
            }
            context.showFailTipDialog(msg)
        }

        private fun handleHttpException(context: Context, exception: HttpException) {
            try {
                val tip: String
                val json = exception.response()?.errorBody()?.string()
                val response = Gson().fromJson(json, Response::class.java)
                var msg = response.data.toString()
                if (msg.isBlank()) {
                    msg = "未知错误"
                }
                tip = when (exception.code()) {
                    400 ->
                        msg
                    401 -> {
                        QMUIDialog.MessageDialogBuilder(context)
                            .setCancelable(false)
                            .setCanceledOnTouchOutside(false)
                            .setMessage("认证失败,请重新登录")
                            .addAction("确定") { dialog, _ ->
                                dialog.dismiss()
                                LoginActivity.start(context)
                            }
                            .show()
                        return
                    }

                    else -> "服务器异常"

                }
                context.showFailTipDialog(tip)
            } catch (e: Exception) {
                context.showFailTipDialog("系统异常: $e")
            }
        }
    }
}