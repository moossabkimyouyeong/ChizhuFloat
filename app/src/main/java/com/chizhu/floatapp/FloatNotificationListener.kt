package com.chizhu.floatapp

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.media.session.MediaController
import android.media.session.MediaSessionManager

class FloatNotificationListener: NotificationListenerService() {
 companion object { var instance:FloatNotificationListener?=null; val recent=ArrayDeque<String>() }
 override fun onListenerConnected() { super.onListenerConnected(); instance=this }
 override fun onDestroy() { if(instance===this) instance=null; super.onDestroy() }
 override fun onNotificationPosted(sbn:StatusBarNotification?) {
  val n=sbn?.notification ?: return
  val title=n.extras.getCharSequence("android.title")?.toString().orEmpty()
  val body=n.extras.getCharSequence("android.text")?.toString().orEmpty()
  if(title.isNotBlank() && sbn.packageName!=packageName) synchronized(recent) { recent.addFirst("$title: $body"); while(recent.size>30) recent.removeLast() }
 }
 fun media():MediaController? = try { (getSystemService(MEDIA_SESSION_SERVICE) as MediaSessionManager).getActiveSessions(android.content.ComponentName(this,FloatNotificationListener::class.java)).firstOrNull() } catch(_:Exception){null}
}
