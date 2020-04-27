package jp.techacademy.kosuke.task_app

import java.io.Serializable
import java.util.Date
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import android.provider.ContactsContract.CommonDataKinds.Email
import io.realm.RealmList



open class Category : RealmObject(), Serializable {
    var category: String = ""   // カテゴリー



    // id をプライマリーキーとして設定
    @PrimaryKey
    var id: Int = 0
}