package jp.techacademy.kosuke.task_app

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_category.*


class CategoryActivity: AppCompatActivity() {


    // Categoryクラスのオブジェクト
    private var mCategory: Category? = null


    private val mOnMakeClickListener = View.OnClickListener {
        addCategory() // Realmに保存/更新

        finish() // InputActivityを閉じて前の画面（MainActivity）に戻る
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)


        // UI部品の設定
        make_categor_button.setOnClickListener(mOnMakeClickListener)


    }
    // Realmに保存/更新するメソッド
    private fun addCategory() {
        val realm = Realm.getDefaultInstance()

        realm.beginTransaction() // （挟む）Realmでデータを追加、削除など変更を行う際に必要

        if (mCategory == null) {
            // 新規作成の場合
            mCategory = Category()

            val categoryRealmResults = realm.where(Category::class.java).findAll()

            val identifier: Int =
                if (categoryRealmResults.max("id") != null) {
                    categoryRealmResults.max("id")!!.toInt() + 1
                } else {
                    0
                }
            mCategory!!.id = identifier
        }


        val category = category_edit_text.text.toString()

        mCategory!!.category = category

        realm.copyToRealmOrUpdate(mCategory!!) // データの保存・更新

        // ↑ 引数で与えたオブジェクト (mTask) が存在していれば更新、なければ追加を行う
        realm.commitTransaction() // （挟む）Realmでデータを追加、削除など変更を行う際に必要

        realm.close()
    }

}