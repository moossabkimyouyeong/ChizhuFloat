package com.chizhu.floatapp

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.*

class MainActivity : Activity() {
 override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState)
  val root = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(36,60,36,24); setBackgroundColor(0xff101727.toInt()) }
  fun label(t:String) = TextView(this).apply { text=t; textSize=18f; setTextColor(-1); setPadding(0,15,0,15) }
  fun button(t:String, action:()->Unit) { root.addView(Button(this).apply { text=t; setOnClickListener { action() } }) }
  root.addView(label("Chizhu Float  •  다크 글래스"))
  root.addView(label("다른 앱 위에 표시 권한을 허용한 뒤 시작하세요. 알림 접근 권한은 알림·음악 기능에 필요합니다."))
  button("① 오버레이 권한 허용") { startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))) }
  button("② 알림 접근 권한 설정") { startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) }
  button("③ 플로팅 버튼 시작") {
   if (!Settings.canDrawOverlays(this)) { Toast.makeText(this,"먼저 오버레이 권한을 허용하세요",Toast.LENGTH_LONG).show(); return@button }
   if (Build.VERSION.SDK_INT>=33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=android.content.pm.PackageManager.PERMISSION_GRANTED) requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS),10)
   try { startForegroundService(Intent(this,FloatService::class.java)) } catch(e:Exception) { Toast.makeText(this,e.message,Toast.LENGTH_LONG).show() }
  }
  button("플로팅 버튼 종료") { stopService(Intent(this,FloatService::class.java)) }
  setContentView(root)
 }
}
