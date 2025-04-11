package com.example.pizza3ps.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.pizza3ps.model.CartData
import com.example.pizza3ps.model.EventData
import com.example.pizza3ps.model.FoodData
import com.example.pizza3ps.model.UserData

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "Pizza3PsDatabase.db"
        private const val DATABASE_VERSION = 5
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createFoodTable = """
            CREATE TABLE IF NOT EXISTS Food (
                id INTEGER PRIMARY KEY,
                name_en TEXT,
                name_vi TEXT,
                price TEXT,
                category TEXT,
                img_path TEXT,
                ingredients TEXT
            )
        """.trimIndent()

        val createCartTable = """
            CREATE TABLE IF NOT EXISTS Cart (
                id INTEGER PRIMARY KEY,
                food_id INTEGER,
                price REAL,
                ingredients TEXT,
                size TEXT,
                crust TEXT,
                crust_base TEXT,
                quantity INTEGER,
                user_email TEXT,
                
                FOREIGN KEY (food_id) REFERENCES Food(id)
            );
        """.trimIndent()

        val createEventTable = """
            CREATE TABLE IF NOT EXISTS Event (
                id INTEGER PRIMARY KEY,
                name TEXT,
                img_path TEXT,
                description TEXT
            )
        """.trimIndent()

        val createUserTable = """
            CREATE TABLE IF NOT EXISTS User (
                email TEXT PRIMARY KEY,
                name TEXT,
                phone TEXT,
                address TEXT,
                points INTEGER
            );
        """.trimIndent()

        db.execSQL(createFoodTable)
        db.execSQL(createCartTable)
        db.execSQL(createEventTable)
        db.execSQL(createUserTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS Food")
        db.execSQL("DROP TABLE IF EXISTS Cart")
        db.execSQL("DROP TABLE IF EXISTS Event")
        db.execSQL("DROP TABLE IF EXISTS User")
        onCreate(db)
    }

    // ===== FOOD TABLE =====
    fun addFood(food: FoodData) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name_en", food.name_en)
            put("name_vi", food.name_vi)
            put("price", food.price)
            put("category", food.category)
            put("img_path", food.imgPath)
            put("ingredients", food.ingredients.joinToString(","))
        }
        db.insert("Food", null, values)
        db.close()
    }

    fun getFoodByCategory(category: String): List<FoodData> {
        val foodList = mutableListOf<FoodData>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM Food WHERE category = ?", arrayOf(category))
        if (cursor.moveToFirst()) {
            do {
                val name_en = cursor.getString(cursor.getColumnIndexOrThrow("name_en"))
                val name_vi = cursor.getString(cursor.getColumnIndexOrThrow("name_vi"))
                val price = cursor.getString(cursor.getColumnIndexOrThrow("price"))
                val imgPath = cursor.getString(cursor.getColumnIndexOrThrow("img_path"))
                val ingredients = cursor.getString(cursor.getColumnIndexOrThrow("ingredients")).split(",").map { it.trim() }
                foodList.add(FoodData(name_en, name_vi, price, category, imgPath, ingredients))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return foodList
    }

    fun getFoodById(id: Int): FoodData {
        var food = FoodData("", "", "", "", "", emptyList())
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM Food WHERE id = ?", arrayOf(id.toString()))
        if (cursor.moveToFirst()) {
            val name_en = cursor.getString(cursor.getColumnIndexOrThrow("name_en"))
            val name_vi = cursor.getString(cursor.getColumnIndexOrThrow("name_vi"))
            val price = cursor.getString(cursor.getColumnIndexOrThrow("price"))
            val imgPath = cursor.getString(cursor.getColumnIndexOrThrow("img_path"))
            val category = cursor.getString(cursor.getColumnIndexOrThrow("category"))
            val ingredients = cursor.getString(cursor.getColumnIndexOrThrow("ingredients")).split(",").map { it.trim() }
            food = FoodData(name_en, name_vi, price, category, imgPath, ingredients)
        }
        cursor.close()
        db.close()
        return food
    }

    fun getFoodId(name: String): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT id FROM Food WHERE name_en = ? OR name_vi = ?", arrayOf(name, name))
        var foodId = -1
        if (cursor.moveToFirst()) {
            foodId = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
        }
        cursor.close()
        db.close()
        return foodId
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

    // ===== CART TABLE =====
    fun addFoodToCart(cartItem: CartData) {
        val db = writableDatabase

        // Kiểm tra xem món đã có trong giỏ chưa
        val ingredientsStr = cartItem.ingredients.sorted().joinToString(",")
        val query = """
            SELECT * FROM Cart
            WHERE food_id = ?
            AND price = ?
            AND ingredients = ?
            AND size = ?
            AND crust = ?
            AND crust_base = ?
        """.trimIndent()

        val cursor = db.rawQuery(
            query,
            arrayOf(
                cartItem.food_id.toString(),
                cartItem.price.toString(),
                ingredientsStr,
                cartItem.size,
                cartItem.crust,
                cartItem.crustBase
            )
        )
        if (cursor.count >0) {
            // Nếu đã có item này trong cart, tăng số lượng lên 1
            cursor.moveToFirst()
            val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val newQuantity = cursor.getInt(cursor.getColumnIndexOrThrow("quantity")) + 1

            val values = ContentValues().apply {
                put("quantity", newQuantity)
            }
            db.update("Cart", values, "id = ?", arrayOf(id.toString()))
        } else {
            // Nếu không có, thêm mới vào giỏ
            val values = ContentValues().apply {
                put("food_id", cartItem.food_id)
                put("price", cartItem.price)
                put("ingredients", ingredientsStr)
                put("size", cartItem.size)
                put("crust", cartItem.crust)
                put("crust_base", cartItem.crustBase)
                put("quantity", cartItem.quantity)
                put("user_email", cartItem.user_email)
            }
            db.insert("Cart", null, values)
        }
        cursor.close()
        db.close()
    }

    fun updateCartItemQuantity(id: Int, quantity: Int) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("quantity", quantity)
        }
        db.update("Cart", values, "id = ?", arrayOf(id.toString()))
        db.close()
    }

    fun getIdOfCartItem(cartItem: CartData): Int {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT id FROM Cart " +
                    "WHERE food_id = ?" +
                    "AND price = ?" +
                    "AND ingredients = ?" +
                    "AND size = ?" +
                    "AND crust = ?" +
                    "AND crust_base = ?" +
                    "AND user_email = ?",
            arrayOf(cartItem.food_id.toString(), cartItem.price.toString(), cartItem.ingredients.sorted().joinToString(","),
                cartItem.size, cartItem.crust, cartItem.crustBase, cartItem.user_email)
        )
        var id = -1
        if (cursor.moveToFirst()) {
            id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
        }
        cursor.close()
        db.close()
        return id
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
                val food_id = cursor.getString(cursor.getColumnIndexOrThrow("food_id")).toInt()
                val price = cursor.getString(cursor.getColumnIndexOrThrow("price")).toInt()
                val ingredients = cursor.getString(cursor.getColumnIndexOrThrow("ingredients")).split(",").map { it.trim() }
                val size = cursor.getString(cursor.getColumnIndexOrThrow("size"))
                val crust = cursor.getString(cursor.getColumnIndexOrThrow("crust"))
                val crustBase = cursor.getString(cursor.getColumnIndexOrThrow("crust_base"))
                val quantity = cursor.getInt(cursor.getColumnIndexOrThrow("quantity"))
                val user_email = cursor.getString(cursor.getColumnIndexOrThrow("user_email"))

                cartList.add(CartData(food_id, price, ingredients, size, crust, crustBase, quantity, user_email))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return cartList
    }

    fun calculateTotalPrice(): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT SUM(price * quantity) FROM Cart", null)
        var totalPrice = 0
        if (cursor.moveToFirst()) {
            totalPrice = cursor.getInt(0)
        }
        cursor.close()
        db.close()
        return totalPrice
    }

    fun deleteCartItem(id: Int) {
        val db = writableDatabase
        db.delete("Cart", "id = ?", arrayOf(id.toString()))
        db.close()
    }

    fun deleteAllCart() {
        val db = writableDatabase

        // Kểm tra có bảng Cart không
        val cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='Cart'", null)
        if (cursor.count > 0) {
            db.execSQL("DELETE FROM Cart")
        }

        cursor.close()
        db.close()
    }

    // ===== USER TABLE =====
    fun addUser(user: UserData) {
        val db = writableDatabase
        db.delete("User", null, null) // Optional: keep latest only
        val values = ContentValues().apply {
            put("email", user.email)
            put("name", user.name)
            put("phone", user.phone)
            put("address", user.address)
            put("points", user.points)
        }
        db.insert("User", null, values)
        db.close()
    }

    fun getUser(): UserData? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM User LIMIT 1", null)
        var user: UserData? = null

        if (cursor.moveToFirst()) {
            val email = cursor.getString(cursor.getColumnIndexOrThrow("email"))
            val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            val phone = cursor.getString(cursor.getColumnIndexOrThrow("phone"))
            val address = cursor.getString(cursor.getColumnIndexOrThrow("address"))
            val points = cursor.getInt(cursor.getColumnIndexOrThrow("points"))

            user = UserData(email, name, phone, address, points)
        }
        cursor.close()
        db.close()
        return user
    }
}