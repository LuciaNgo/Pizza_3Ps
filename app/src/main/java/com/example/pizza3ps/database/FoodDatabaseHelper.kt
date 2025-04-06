package com.example.pizza3ps.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.pizza3ps.model.FoodData

class FoodDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "FoodDatabase.db"
        private const val DATABASE_VERSION = 3

        const val TABLE_NAME = "Food"
        const val COLUMN_ID = "_id"
        const val COLUMN_NAME = "name"
        const val COLUMN_PRICE = "price"
        const val COLUMN_CATEGORY = "category"
        const val COLUMN_IMG_PATH = "img_path"
        const val COLUMN_INGREDIENTS = "ingredients"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NAME TEXT,
                $COLUMN_PRICE TEXT,
                $COLUMN_CATEGORY TEXT,
                $COLUMN_IMG_PATH TEXT,
                $COLUMN_INGREDIENTS TEXT
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun addFood(food: FoodData) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, food.name)
            put(COLUMN_PRICE, food.price)
            put(COLUMN_CATEGORY, food.category)
            put(COLUMN_IMG_PATH, food.imgPath)
            put(COLUMN_INGREDIENTS, food.ingredients.joinToString(","))
        }
        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    fun getAllFood(): List<FoodData> {
        val foodList = mutableListOf<FoodData>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME", null)

        if (cursor.moveToFirst()) {
            do {
                val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME))
                val price = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRICE))
                val category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY))
                val imgPath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMG_PATH))
                val ingredients = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_INGREDIENTS)).split(",").map { it.trim() }

                foodList.add(FoodData(name, price, category, imgPath, ingredients))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return foodList
    }

    fun getFoodByCategory(category: String): List<FoodData> {
        val foodList = mutableListOf<FoodData>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME WHERE $COLUMN_CATEGORY = ?", arrayOf(category))

        if (cursor.moveToFirst()) {
            do {
                val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME))
                val price = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRICE))
                val imgPath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMG_PATH))
                val ingredients = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_INGREDIENTS)).split(",").map { it.trim() }

                foodList.add(FoodData(name, price, category, imgPath, ingredients))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return foodList
    }

    fun searchFoodByName(name: String): List<FoodData> {
        val foodList = mutableListOf<FoodData>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME WHERE $COLUMN_NAME LIKE ?", arrayOf("%$name%"))

        if (cursor.moveToFirst()) {
            do {
                val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME))
                val price = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRICE))
                val category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY))
                val imgPath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMG_PATH))
                val ingredients = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_INGREDIENTS)).split(",").map { it.trim() }

                foodList.add(FoodData(name, price, category, imgPath, ingredients))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return foodList
    }

    fun deleteAllFood() {
        val db = writableDatabase
        db.delete("Food", null, null)
        db.close()
    }
}