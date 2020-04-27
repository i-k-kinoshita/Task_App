package jp.techacademy.kosuke.task_app

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.View
import io.realm.Realm
import kotlinx.android.synthetic.main.content_input.*
import java.util.*
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_category.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.collections.ArrayList

class InputActivity : AppCompatActivity() {

    // タスクの日時を保持するもの
    private var mYear = 0
    private var mMonth = 0
    private var mDay = 0
    private var mHour = 0
    private var mMinute = 0

    // Taskクラスのオブジェクト
    private var mTask: Task? = null

    // Categoryクラスのオブジェクト
    private var mCategory: Category? = null


    // 日付を設定するButtonのリスナー
    private val mOnDateClickListener = View.OnClickListener {
        val datePickerDialog = DatePickerDialog(this,
            DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                mYear = year
                mMonth = month
                mDay = dayOfMonth
                val dateString = mYear.toString() + "/" + String.format("%02d", mMonth + 1) + "/" + String.format("%02d", mDay)
                date_button.text = dateString
            }, mYear, mMonth, mDay)
        datePickerDialog.show()
    }

    // 時間を設定するButtonのリスナー
    private val mOnTimeClickListener = View.OnClickListener {
        val timePickerDialog = TimePickerDialog(this,
            TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                mHour = hour
                mMinute = minute
                val timeString = String.format("%02d", mHour) + ":" + String.format("%02d", mMinute)
                times_button.text = timeString
            }, mHour, mMinute, false)
        timePickerDialog.show()
    }

    // 決定Buttonのリスナー
    private val mOnDoneClickListener = View.OnClickListener {
        addTask() // Realmに保存/更新
        finish() // InputActivityを閉じて前の画面（MainActivity）に戻る
    }


    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input)

        // UI部品の設定
        date_button.setOnClickListener(mOnDateClickListener)
        times_button.setOnClickListener(mOnTimeClickListener)
        done_button.setOnClickListener(mOnDoneClickListener)

        make_button.setOnClickListener { view ->
            val intent = Intent(this@InputActivity, CategoryActivity::class.java)
            startActivity(intent)
        }


        // EXTRA_TASK から Task の id を取得して、 id から Task のインスタンスを取得する
        val intent = intent
        // Task の id を取り出す
        val taskId = intent.getIntExtra(EXTRA_TASK, -1)
        val realm = Realm.getDefaultInstance()
        //  Task の id が taskId のものが検索され、findFirst() によって最初に見つかったインスタンスが返される
        // 新規作成の場合、id には -1 （mTask = null）が入る
        mTask = realm.where(Task::class.java).equalTo("id", taskId).findFirst()


        realm.close()

        // 新規作成の場合
        if (mTask == null) {
            val calendar = Calendar.getInstance()
            mYear = calendar.get(Calendar.YEAR)
            mMonth = calendar.get(Calendar.MONTH)
            mDay = calendar.get(Calendar.DAY_OF_MONTH)
            mHour = calendar.get(Calendar.HOUR_OF_DAY)
            mMinute = calendar.get(Calendar.MINUTE)
        } else {
            // 更新の場合
            title_edit_text.setText(mTask!!.title)
 //           spinner2.setLayerType(mCategory!!.category)
            content_edit_text.setText(mTask!!.contents)

            val calendar = Calendar.getInstance()
            calendar.time = mTask!!.date
            mYear = calendar.get(Calendar.YEAR)
            mMonth = calendar.get(Calendar.MONTH)
            mDay = calendar.get(Calendar.DAY_OF_MONTH)
            mHour = calendar.get(Calendar.HOUR_OF_DAY)
            mMinute = calendar.get(Calendar.MINUTE)

            val dateString = mYear.toString() + "/" + String.format("%02d", mMonth + 1) + "/" + String.format("%02d", mDay)
            val timeString = String.format("%02d", mHour) + ":" + String.format("%02d", mMinute)

            date_button.text = dateString
            times_button.text = timeString
        }
    }

    override fun onResume() {
        super.onResume()


        val realm = Realm.getDefaultInstance()


        val categoryRealmResults = realm.where(Category::class.java).findAll()
        //val categoryRealmResults: ArrayList<Category>

        var mutableList = mutableListOf<String>()
//

        categoryRealmResults.forEach(){
            mutableList.add(it.category)
        }


//        val length = categoryRealmResults.count()
//        var count = 0
//        while (count < length) {
//            mutableList.add(categoryRealmResults[count]!!.category)
//            count++
//        }


        //アダプターを設定
        val adapter = ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_item,
            mutableList
        )

        spinner2.adapter = adapter

        realm.close()

    }

    // Realmに保存/更新するメソッド
    private fun addTask() {
        val realm = Realm.getDefaultInstance()

        realm.beginTransaction() // （挟む）Realmでデータを追加、削除など変更を行う際に必要

        if (mTask == null) {
            // 新規作成の場合
            mTask = Task()
//            mCategory = Category()

            val taskRealmResults = realm.where(Task::class.java).findAll()

            val identifier: Int =
                if (taskRealmResults.max("id") != null) {
                    taskRealmResults.max("id")!!.toInt() + 1
                } else {
                    0
                }
            mTask!!.id = identifier
//            mCategory!!.id = identifier
        }

        val title = title_edit_text.text.toString()
        val content = content_edit_text.text.toString()
        val category = spinner2.selectedItem.toString()

        mTask!!.title = title
        mTask!!.contents = content
        val calendar = GregorianCalendar(mYear, mMonth, mDay, mHour, mMinute)
        val date = calendar.time
        mTask!!.date = date
        mTask!!.category = category

//        mCategory!!.category = category


        realm.copyToRealmOrUpdate(mTask!!) // データの保存・更新
//        realm.copyToRealmOrUpdate(mCategory!!) // データの保存・更新

        // ↑ 引数で与えたオブジェクト (mTask) が存在していれば更新、なければ追加を行う
        realm.commitTransaction() // （挟む）Realmでデータを追加、削除など変更を行う際に必要

        realm.close()

        // id はそのままで、データの内容だけ変更？？
        // TaskAlarmReceiver：通知機能
        val resultIntent = Intent(applicationContext, TaskAlarmReceiver::class.java)
        resultIntent.putExtra(EXTRA_TASK, mTask!!.id)
        val resultPendingIntent = PendingIntent.getBroadcast(
            this,
            mTask!!.id,
            resultIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
            // PendingIntent：通知機能を呼び出している
            // 記述されたPendingIntentがすでに存在する場合、それを保持しますが、
            // その追加のデータをこの新しいインテントにあるもので置き換えることを示すフラグ
        )

        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, resultPendingIntent)
        // 第一引数のRTC_WAKEUP：「UTC時間を指定する。画面スリープ中でもアラームを発行する」
        // 第二引数でタスクの時間をUTC時間で指定
    }
}