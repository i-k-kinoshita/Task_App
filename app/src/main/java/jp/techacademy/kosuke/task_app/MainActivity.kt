package jp.techacademy.kosuke.task_app

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_main.*
import io.realm.RealmChangeListener
import io.realm.Sort
import android.content.Intent
import android.support.v7.app.AlertDialog
import android.app.AlarmManager
import android.app.PendingIntent
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.content_input.*

const val EXTRA_TASK = "jp.techacademy.kosuke.task_app.TASK"

class MainActivity : AppCompatActivity() {
    //Realmクラスを保持するmRealmを定義
    private lateinit var mRealm: Realm

//    private var mCategory: Category? = null


    //データベースに追加や削除など変化があった場合に呼ばれるリスナー
    private val mRealmListener = object : RealmChangeListener<Realm> {
        override fun onChange(element: Realm) {
            reloadListView()
        }
    }

    private lateinit var mTaskAdapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fab.setOnClickListener { view ->
            val intent = Intent(this@MainActivity, InputActivity::class.java)
            startActivity(intent)
        }

//        mCategory!!.category = "仕事"
//        spinner1.adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item).apply { mCategory!!.category }


        // Realmの設定
        mRealm = Realm.getDefaultInstance()  //オブジェクトを取得
        mRealm.addChangeListener(mRealmListener)  //mRealmListenerをaddChangeListenerメソッドで設定

        // ListViewの設定
        mTaskAdapter = TaskAdapter(this@MainActivity)

        // ListViewをタップしたときの処理
        listView1.setOnItemClickListener { parent, _, position, _ ->
            // 入力・編集する画面に遷移させる
            val task = parent.adapter.getItem(position) as Task
            val intent = Intent(this@MainActivity, InputActivity::class.java)
            intent.putExtra(EXTRA_TASK, task.id)
            startActivity(intent)
        }

        // ListViewを長押ししたときの処理
        listView1.setOnItemLongClickListener { parent, _, position, _ ->
            // タスクを削除する
            val task = parent.adapter.getItem(position) as Task

            // ダイアログを表示する
            val builder = AlertDialog.Builder(this@MainActivity)


            builder.setTitle("削除")
            builder.setMessage(task.title + "を削除しますか")

            builder.setPositiveButton("OK"){_, _ ->
                // 選択したセルに該当するタスクと同じIDのものを検索
                val results = mRealm.where(Task::class.java).equalTo("id", task.id).findAll()

                mRealm.beginTransaction() //↓を挟む
                results.deleteAllFromRealm() // 長押ししたタスクを削除
                mRealm.commitTransaction() //↑を挟む

                val resultIntent = Intent(applicationContext, TaskAlarmReceiver::class.java)
                val resultPendingIntent = PendingIntent.getBroadcast(
                    this@MainActivity,
                    task.id,
                    resultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )

                // アラームを解除
                val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
                alarmManager.cancel(resultPendingIntent)

                reloadListView()
            }

            builder.setNegativeButton("CANCEL", null)

            val dialog = builder.create()
            dialog.show()

            true
        }

        //カテゴリー検索
        search_button.setOnClickListener {
            val category  = spinner1.selectedItem.toString()

            if(category == ""){
                reloadListView()
            }else{
                val results2 = mRealm.where(Task::class.java).equalTo("category", category).findAll()
                // 上記の結果を、TaskList としてセットする
                mTaskAdapter.taskList = mRealm.copyFromRealm(results2)

                // TaskのListView用のアダプタに渡す
                listView1.adapter = mTaskAdapter

                // 表示を更新するために、アダプターにデータが変更されたことを知らせる
                mTaskAdapter.notifyDataSetChanged()
            }
        }
        reloadListView()

    }

    override fun onResume() {
        super.onResume()

        val realm = Realm.getDefaultInstance()
        val categoryRealmResults = realm.where(Category::class.java).findAll()
        //val categoryRealmResults: ArrayList<Category>
        var mutableList = mutableListOf<String>()

        categoryRealmResults.forEach(){
            mutableList.add(it.category)
        }
        //アダプターを設定
        val adapter = ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_item,
            mutableList
        )
        spinner1.adapter = adapter
        realm.close()
    }

    private fun reloadListView() {
        // Realmデータベースから、「全てのデータ(findAll)を取得して新しい日時順に並べた結果」を取得
        // sortで、"date" （日時）を Sort.DESCENDING （降順）で並べ替えた結果を返す
        val taskRealmResults = mRealm.where(Task::class.java).findAll().sort("date", Sort.DESCENDING)

        // 上記の結果を、TaskList としてセットする
        mTaskAdapter.taskList = mRealm.copyFromRealm(taskRealmResults)

        // TaskのListView用のアダプタに渡す
        listView1.adapter = mTaskAdapter

        // 表示を更新するために、アダプターにデータが変更されたことを知らせる
        mTaskAdapter.notifyDataSetChanged()
    }

    override fun onDestroy() {
        super.onDestroy()

        // Activityが破棄されるときにRealmクラスのオブジェクトを破棄
        mRealm.close()
    }
}