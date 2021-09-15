package br.com.santander.afe.poc_connector

import android.os.Handler
import android.os.Looper
import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*

/** PocConnectorPlugin */
class PocConnectorPlugin : FlutterPlugin, MethodCallHandler {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private lateinit var channel: MethodChannel
  private val mainScope = CoroutineScope(Dispatchers.Main)
  private val handler: Handler by lazy { Handler(Looper.getMainLooper()) }
  override fun onAttachedToEngine(
      @NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding
  ) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "poc_connector")
    channel.setMethodCallHandler(this)
  }

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    if (call.method == "getPlatformVersion") {
      handler.post { result.success("Android ${android.os.Build.VERSION.RELEASE}") }
    } else if (call.method == "connectorGet") {
      val url = call.argument<String>("url")
      mainScope.launch {
        try {
          withContext(Dispatchers.Default) { get(url!!, result) }
        } catch (e: Exception) {
          println("Erro:")
          println(e)
          result.error("LoginException", e.message, null)
        }
      }
    } else {
      result.notImplemented()
    }
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }

  suspend fun get(@NonNull url: String, @NonNull result: Result) {

    val trustAllCerts: Array<TrustManager> =
        arrayOf<TrustManager>(
            object : X509TrustManager {
              @Throws(CertificateException::class)
              override fun checkClientTrusted(chain: Array<X509Certificate?>?, authType: String?) {}

              @Throws(CertificateException::class)
              override fun checkServerTrusted(chain: Array<X509Certificate?>?, authType: String?) {}

              override fun getAcceptedIssuers(): Array<X509Certificate?>? {
                return arrayOf()
              }
            }
        )

    // Install the all-trusting trust manager
    val sslContext: SSLContext = SSLContext.getInstance("SSL")
    sslContext.init(null, trustAllCerts, SecureRandom())

    // Create an ssl socket factory with our all-trusting manager
    val sslSocketFactory: SSLSocketFactory = sslContext.socketFactory

    val httpClient =
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier(Verifier())
            .build()

    val builder: Request.Builder = Request.Builder()
    builder.url(url)

    val request: Request = builder.build()

    val response: Response = httpClient.newCall(request).execute()

    val jsonData: String = response.body?.string() ?: "{'content': []}"

    // result.success("{\"content\":[{\"id\":1,\"name\":\"Superman\",\"description\":\"Mais rápido
    // que uma bala, mais poderoso do que uma locomotiva, capaz de saltar prédios altos em um único
    // salto.\"}]}");
    handler.post { result.success(jsonData) }
  }
}

fun Result.onMain(): ResultOnMain {
  return if (this is ResultOnMain) {
    this
  } else {
    ResultOnMain(this)
  }
}

class ResultOnMain internal constructor(private val result: Result) : Result {
  private val handler: Handler by lazy { Handler(Looper.getMainLooper()) }

  override fun success(res: Any?) {
    handler.post { result.success(res) }
  }

  override fun error(errorCode: String, errorMessage: String?, errorDetails: Any?) {
    handler.post { result.error(errorCode, errorMessage, errorDetails) }
  }

  override fun notImplemented() {
    handler.post { result.notImplemented() }
  }
}
