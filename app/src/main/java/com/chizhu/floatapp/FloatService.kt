package com.chizhu.floatapp

import android.app.*
import android.content.*
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.os.*
import android.view.*
import android.webkit.*
import android.widget.*

class FloatService: Service() {
 private lateinit var wm:WindowManager
 private var bubble:View?=null
 private var panel:View?=null
 private val handler=Handler(Looper.getMainLooper())
 private fun dp(n:Int)=(n*resources.displayMetrics.density).toInt()
 private fun bg()=GradientDrawable().apply {setColor(0xee131c30.toInt());cornerRadius=dp(20).toFloat()}
 private fun text(s:String)=TextView(this).apply {text=s;textSize=17f;setTextColor(-1);setPadding(dp(10),dp(10),dp(10),dp(10))}
 private fun button(label:String,action:()->Unit)=Button(this).apply {text=label;isAllCaps=false;setOnClickListener{action()}}
 private fun field(hint:String)=EditText(this).apply {setTextColor(-1);setHintTextColor(0xffaabbcc.toInt());setHint(hint)}
 override fun onBind(i:Intent?)=null
 override fun onCreate(){super.onCreate();wm=getSystemService(WINDOW_SERVICE) as WindowManager
  val nm=getSystemService(NOTIFICATION_SERVICE) as NotificationManager
  nm.createNotificationChannel(NotificationChannel("float","Chizhu Float",NotificationManager.IMPORTANCE_LOW))
  val notification=Notification.Builder(this,"float").setContentTitle("Chizhu Float").setContentText("플로팅 도구 실행 중").setSmallIcon(android.R.drawable.ic_menu_manage).build()
  startForeground(1,notification)
  val b=text("✦").apply {textSize=30f;gravity=Gravity.CENTER;background=bg()}
  val p=WindowManager.LayoutParams(dp(60),dp(60),WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,PixelFormat.TRANSLUCENT).apply{gravity=Gravity.TOP or Gravity.START;x=dp(20);y=dp(200)}
  wm.addView(b,p);bubble=b
  var sx=0f;var sy=0f;var px=0;var py=0;var moved=false
  b.setOnTouchListener { _,e -> when(e.action){
   MotionEvent.ACTION_DOWN->{sx=e.rawX;sy=e.rawY;px=p.x;py=p.y;moved=false;true}
   MotionEvent.ACTION_MOVE->{if(kotlin.math.abs(e.rawX-sx)>dp(6)||kotlin.math.abs(e.rawY-sy)>dp(6))moved=true;p.x=px+(e.rawX-sx).toInt();p.y=py+(e.rawY-sy).toInt();wm.updateViewLayout(b,p);true}
   MotionEvent.ACTION_UP->{if(!moved)menu();true}
   else->true
  }}
 }
 private fun window(title:String,fill:(LinearLayout)->Unit){
  panel?.let{wm.removeView(it)}
  val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;background=bg();setPadding(dp(10),dp(10),dp(10),dp(10))}
  val top=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL}
  top.addView(text(title),LinearLayout.LayoutParams(0,dp(55),1f))
  top.addView(button("✕"){wm.removeView(root);panel=null})
  root.addView(top)
  val scroll=ScrollView(this);val body=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL}
  scroll.addView(body);root.addView(scroll,LinearLayout.LayoutParams(-1,0,1f));fill(body)
  val p=WindowManager.LayoutParams((resources.displayMetrics.widthPixels-dp(30)).coerceAtMost(dp(390)),(resources.displayMetrics.heightPixels-dp(120)).coerceAtMost(dp(610)),WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,PixelFormat.TRANSLUCENT).apply{gravity=Gravity.TOP or Gravity.START;x=dp(15);y=dp(80);softInputMode=WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE}
  wm.addView(root,p);panel=root
  var sx=0f;var sy=0f;var px=0;var py=0
  top.setOnTouchListener{_,e->when(e.action){MotionEvent.ACTION_DOWN->{sx=e.rawX;sy=e.rawY;px=p.x;py=p.y;true};MotionEvent.ACTION_MOVE->{p.x=px+(e.rawX-sx).toInt();p.y=py+(e.rawY-sy).toInt();wm.updateViewLayout(root,p);true};else->true}}
 }
 private fun menu(): Unit = window("Chizhu Float"){c->
  listOf("브라우저","메모장","계산기","타이머·스톱워치","클립보드","음악 컨트롤러","앱 바로가기","최근 알림").forEachIndexed{i,n->c.addView(button(n){when(i){0->browser();1->notes();2->calculator();3->timer();4->clipboard();5->music();6->apps();7->notifications()}})}
 }
 private fun browser()=window("브라우저"){c->
  val address=field("웹 주소");c.addView(address)
  val web=WebView(this).apply{settings.javaScriptEnabled=true;settings.domStorageEnabled=true;webViewClient=WebViewClient();loadUrl("https://www.google.com")}
  c.addView(button("이동"){val s=address.text.toString().trim();if(s.isNotBlank())web.loadUrl(if(s.startsWith("https://"))s else "https://"+s)})
  c.addView(web,LinearLayout.LayoutParams(-1,dp(400)))
 }
 private fun notes()=window("메모장"){c->
  val pref=getSharedPreferences("chizhu",MODE_PRIVATE)
  val e=field("메모").apply{setText(pref.getString("note",""));minLines=8;gravity=Gravity.TOP};c.addView(e)
  c.addView(button("저장"){pref.edit().putString("note",e.text.toString()).apply();Toast.makeText(this,"저장됨",Toast.LENGTH_SHORT).show()})
 }
 private fun calculator()=window("계산기"){c->
  val display=field("0");c.addView(display)
  var left=0.0;var op="+";var clear=false
  listOf(listOf("7","8","9","÷"),listOf("4","5","6","×"),listOf("1","2","3","-"),listOf("C","0",".","+"),listOf("=")).forEach{line->
   val row=LinearLayout(this);c.addView(row)
   line.forEach{k->row.addView(button(k){
    when(k){
     "C"->{display.setText("");left=0.0}
     "+","-","×","÷"->{left=display.text.toString().toDoubleOrNull()?:0.0;op=k;clear=true}
     "="->{val right=display.text.toString().toDoubleOrNull()?:0.0;val r=when(op){"+"->left+right;"-"->left-right;"×"->left*right;else->if(right==0.0)Double.NaN else left/right};display.setText(r.toString());clear=true}
     else->{if(clear){display.setText("");clear=false};display.append(k)}
    }
   },LinearLayout.LayoutParams(0,dp(58),1f))}
  }
 }
 private fun timer()=window("타이머 · 스톱워치"){c->
  val display=text("00:00").apply{textSize=30f};c.addView(display)
  val seconds=field("초 단위");seconds.inputType=2;c.addView(seconds)
  var started=0L;var duration=0L;var countdown=false;var running=false
  val tick=object:Runnable{override fun run(){if(!running)return;val elapsed=(SystemClock.elapsedRealtime()-started)/1000;val n=if(countdown)(duration-elapsed).coerceAtLeast(0) else elapsed;display.text="%02d:%02d".format(n/60,n%60);if(countdown&&n==0L){running=false;return};handler.postDelayed(this,250)}}
  c.addView(button("타이머 시작"){duration=seconds.text.toString().toLongOrNull()?.coerceIn(1,86400)?:60;started=SystemClock.elapsedRealtime();countdown=true;running=true;handler.removeCallbacks(tick);handler.post(tick)})
  c.addView(button("스톱워치 시작"){started=SystemClock.elapsedRealtime();countdown=false;running=true;handler.removeCallbacks(tick);handler.post(tick)})
  c.addView(button("정지"){running=false;handler.removeCallbacks(tick)})
 }
 private fun clipboard()=window("클립보드"){c->
  val pref=getSharedPreferences("chizhu",MODE_PRIVATE)
  val e=field("저장할 문구").apply{setText(pref.getString("clip",""));minLines=4};c.addView(e)
  c.addView(button("저장"){pref.edit().putString("clip",e.text.toString()).apply()})
  c.addView(button("복사"){(getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager).setPrimaryClip(ClipData.newPlainText("Chizhu",e.text.toString()))})
 }
 private fun music()=window("음악 컨트롤러"){c->
  val controller=FloatNotificationListener.instance?.media()
  c.addView(text(if(controller==null)"음악 세션 없음. 알림 접근 권한을 확인하세요." else "연결됨"))
  c.addView(button("이전 곡"){controller?.transportControls?.skipToPrevious()})
  c.addView(button("재생"){controller?.transportControls?.play()})
  c.addView(button("일시정지"){controller?.transportControls?.pause()})
  c.addView(button("다음 곡"){controller?.transportControls?.skipToNext()})
 }
 private fun apps()=window("앱 바로가기"){c->
  val intent=Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
  packageManager.queryIntentActivities(intent,0).sortedBy{it.loadLabel(packageManager).toString()}.take(100).forEach{info->
   c.addView(button(info.loadLabel(packageManager).toString()){packageManager.getLaunchIntentForPackage(info.activityInfo.packageName)?.let{it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);startActivity(it)}})
  }
 }
 private fun notifications(): Unit = window("최근 알림"){c->
  val items=synchronized(FloatNotificationListener.recent){FloatNotificationListener.recent.toList()}
  if(items.isEmpty())c.addView(text("새로 수신한 알림이 없습니다."))
  items.forEach{c.addView(text(it))}
  c.addView(button("새로고침"){notifications()})
 }
 override fun onDestroy(){handler.removeCallbacksAndMessages(null);panel?.let{wm.removeView(it)};bubble?.let{wm.removeView(it)};super.onDestroy()}
}
