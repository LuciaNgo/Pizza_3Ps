package com.example.pizza3ps.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.pizza3ps.model.CartData
import com.example.pizza3ps.model.FoodData

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "Pizza3PsDatabase.db"
        private const val DATABASE_VERSION = 2

        const val COLUMN_ID = "_id"
        const val COLUMN_NAME = "name"
        const val COLUMN_PRICE = "price"
        const val COLUMN_CATEGORY = "category"
        const val COLUMN_IMG_PATH = "img_path"
        const val COLUMN_INGREDIENTS = "ingredients"
        const val COLUMN_SIZE = "size"
        const val COLUMN_CRUST = "crust"
        const val COLUMN_CRUST_BASE = "crust_base"
        const val COLUMN_QUANTITY = "quantity"

    }

    override fun onCreate(db: SQLiteDatabase) {
        val createFoodTable = """
            CREATE TABLE IF NOT EXISTS Food (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NAME TEXT,
                $COLUMN_PRICE TEXT,
                $COLUMN_CATEGORY TEXT,
                $COLUMN_IMG_PATH TEXT,
                $COLUMN_INGREDIENTS TEXT
            )
        """.trimIndent()

        val createCartTable = """
        CREATE TABLE IF NOT EXISTS Cart (
            $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_NAME TEXT,
            $COLUMN_PRICE REAL,
            $COLUMN_CATEGORY TEXT,
            $COLUMN_IMG_PATH TEXT,
            $COLUMN_INGREDIENTS TEXT,
            $COLUMN_SIZE TEXT,
            $COLUMN_CRUST TEXT,
            $COLUMN_CRUST_BASE TEXT,
            $COLUMN_QUANTITY INTEGER
            );
        """.trimIndent()

        db.execSQL(createFoodTable)
        db.execSQL(createCartTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS Food")
        db.execSQL("DROP TABLE IF EXISTS Cart")
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
        db.insert("Food", null, values)
        db.close()
    }

    fun addFoodToCart(cartItem: CartData) {
        val db = writableDatabase

        // Kiểm tra xem món đã có trong giỏ chưa
        val ingredientsStr = cartItem.ingredients.sorted().joinToString(",")
        val query = """
            SELECT * FROM Cart
            WHERE name = ?
            AND price = ?
            AND category = ?
            AND ingredients = ?
            AND size = ?
            AND crust = ?
            AND crust_base = ?
        """.trimIndent()

        val cursor = db.rawQuery(
            query,
            arrayOf(
                cartItem.name,
                cartItem.price.toString(),
                cartItem.category,
                ingredientsStr,
                cartItem.size,
                cartItem.crust,
                cartItem.crustBase
            )
        )
        if (cursor.count >0) {
            // Nếu đã có item này trong cart, tăng số lượng lên 1
            cursor.moveToFirst()
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
            val newQuantity = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_QUANTITY)) + 1

            val values = ContentValues().apply {
                put(COLUMN_QUANTITY, newQuantity)
            }
            db.update("Cart", values, "$COLUMN_ID = ?", arrayOf(id.toString()))
        } else {
            // Nếu không có, thêm mới vào giỏ
            val values = ContentValues().apply {
                put(COLUMN_NAME, cartItem.name)
                put(COLUMN_PRICE, cartItem.price)
                put(COLUMN_CATEGORY, cartItem.category)
                put(COLUMN_IMG_PATH, cartItem.imgPath)
                put(COLUMN_INGREDIENTS, ingredientsStr)
                put(COLUMN_SIZE, cartItem.size)
                put(COLUMN_CRUST, cartItem.crust)
                put(COLUMN_CRUST_BASE, cartItem.crustBase)
                put(COLUMN_QUANTITY, 1)
            }
            db.insert("Cart", null, values)
        }
    }

    fun getCartItemCount(): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM Cart", null)
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()
        db.close()
        return count
    }

    fun getAllCartItems(): List<CartData> {
        val cartList = mutableListOf<CartData>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM Cart", null)

        if (cursor.moveToFirst()) {
            do {
                val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME))
                val price = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRICE)).toInt()
                val category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY))
                val imgPath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMG_PATH))
                val ingredients = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_INGREDIENTS)).split(",").map { it.trim() }
                val size = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SIZE))
                val crust = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CRUST))
                val crustBase = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CRUST_BASE))
                val quantity = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_QUANTITY))

                cartList.add(CartData(name, price, category, imgPath, ingredients, size, crust, crustBase, quantity))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return cartList
    }

    fun getFoodByCategory(category: String): List<FoodData> {
        val foodList = mutableListOf<FoodData>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM Food WHERE $COLUMN_CATEGORY = ?", arrayOf(category))

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

    fun deleteAllFood() {
        val db = writableDatabase
        // Kểm tra có bảng Food không
        val cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='Food'", null)
        if (cursor.count > 0) {
            db.execSQL("DELETE FROM Food")
        }
        cursor.close()
        db.close()
    }
}