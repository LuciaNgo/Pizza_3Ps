package com.example.pizza3ps.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.pizza3ps.model.UserData

class UserDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "FoodDatabase.db"
        private const val DATABASE_VERSION = 2

        const val TABLE_NAME = "User"
        const val COLUMN_ID = "_id"
        const val COLUMN_NAME = "name"
        const val COLUMN_EMAIL = "email"
        const val COLUMN_PHONE = "phone"
        const val COLUMN_ADDRESS = "address"
        const val COLUMN_POINTS = "points"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NAME TEXT,
                $COLUMN_EMAIL TEXT UNIQUE,
                $COLUMN_PHONE TEXT,
                $COLUMN_ADDRESS TEXT,
                $COLUMN_POINTS INTEGER
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun addUser(user: UserData) {
        val db = writableDatabase
        db.delete(TABLE_NAME, null, null) // Optional: keep only latest user
        val values = ContentValues().apply {
            put(COLUMN_NAME, user.name)
            put(COLUMN_EMAIL, user.email)
            put(COLUMN_PHONE, user.phone)
            put(COLUMN_ADDRESS, user.address)
            put(COLUMN_POINTS, user.points)
        }

        db.insert(TABLE_NAME, null, values)

//        if (userExists(user.email)) {
//            // Update user
//            db.update(TABLE_NAME, values, "$COLUMN_EMAIL = ?", arrayOf(user.email))
//        } else {
//            // Insert new
//            db.insert(TABLE_NAME, null, values)
//        }

        db.close()
    }

//    fun userExists(email: String): Boolean {
//        val db = readableDatabase
//        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME WHERE $COLUMN_EMAIL = ?", arrayOf(email))
//        val exists = cursor.count > 0
//        cursor.close()
//        return exists
//    }


    fun getUser(): UserData? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME LIMIT 1", null)
        var user: UserData? = null

        if (cursor.moveToFirst()) {
            val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME))
            val email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL))
            val phone = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE))
            val address = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADDRESS))
            val points = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_POINTS))

            user = UserData(name, email, phone, address, points)
        }

        cursor.close()
        db.close()
        return user
    }
}
