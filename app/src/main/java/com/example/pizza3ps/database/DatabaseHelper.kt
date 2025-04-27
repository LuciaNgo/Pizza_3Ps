package com.example.pizza3ps.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.pizza3ps.model.AddressData
import com.example.pizza3ps.model.CartData
import com.example.pizza3ps.model.EventData
import com.example.pizza3ps.model.FoodData
import com.example.pizza3ps.model.IngredientData
import com.example.pizza3ps.model.RestaurantData
import com.example.pizza3ps.model.UserData

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "Pizza3PsDatabase.db"
        private const val DATABASE_VERSION = 10
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

        val createIngredientTable = """
            CREATE TABLE IF NOT EXISTS Ingredient (
                id INTEGER PRIMARY KEY,
                name TEXT,
                price REAL,
                category TEXT,
                icon_img_path TEXT,
                layer_img_path TEXT
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
                id TEXT PRIMARY KEY,
                email TEXT,
                name TEXT,
                phone TEXT,
                address TEXT,
                points INTEGER
            );
        """.trimIndent()

        val createRestaurantInfoTable = """
            CREATE TABLE IF NOT EXISTS RestaurantInfo (
                id INTEGER PRIMARY KEY,
                name TEXT,
                address TEXT,
                phone TEXT,
                mail TEXT
            )
        """.trimIndent()

        val createAddressTable = """
            CREATE TABLE IF NOT EXISTS Address (
                id INTEGER PRIMARY KEY,
                name TEXT,
                phone TEXT,
                address TEXT,
                isDefault INTEGER
            )
        """.trimIndent()

        db.execSQL(createFoodTable)
        db.execSQL(createIngredientTable)
        db.execSQL(createCartTable)
        db.execSQL(createEventTable)
        db.execSQL(createUserTable)
        db.execSQL(createRestaurantInfoTable)
        db.execSQL(createAddressTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS Food")
        db.execSQL("DROP TABLE IF EXISTS Ingredient")
        db.execSQL("DROP TABLE IF EXISTS Cart")
        db.execSQL("DROP TABLE IF EXISTS Event")
        db.execSQL("DROP TABLE IF EXISTS User")
        db.execSQL("DROP TABLE IF EXISTS RestaurantInfo")
        db.execSQL("DROP TABLE IF EXISTS Address")
        onCreate(db)
    }

    // ===== FOOD TABLE =====
    fun addFood(food: FoodData) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("id", food.id)
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

    fun getAllFood(): List<FoodData> {
        val foodList = mutableListOf<FoodData>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM Food", null)
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("id")).toString()
                val nameEn = cursor.getString(cursor.getColumnIndexOrThrow("name_en"))
                val nameVi = cursor.getString(cursor.getColumnIndexOrThrow("name_vi"))
                val price = cursor.getString(cursor.getColumnIndexOrThrow("price"))
                val imgPath = cursor.getString(cursor.getColumnIndexOrThrow("img_path"))
                val category = cursor.getString(cursor.getColumnIndexOrThrow("category"))
                val ingredients = cursor.getString(cursor.getColumnIndexOrThrow("ingredients")).split(",").map { it.trim() }
                foodList.add(FoodData(id, nameEn, nameVi, price, category, imgPath, ingredients))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return foodList
    }

    fun getFoodByCategory(category: String): List<FoodData> {
        val foodList = mutableListOf<FoodData>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM Food WHERE category = ?", arrayOf(category))
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("id")).toString()
                val nameEn = cursor.getString(cursor.getColumnIndexOrThrow("name_en"))
                val nameVi = cursor.getString(cursor.getColumnIndexOrThrow("name_vi"))
                val price = cursor.getString(cursor.getColumnIndexOrThrow("price"))
                val imgPath = cursor.getString(cursor.getColumnIndexOrThrow("img_path"))
                val ingredients = cursor.getString(cursor.getColumnIndexOrThrow("ingredients")).split(",").map { it.trim() }
                foodList.add(FoodData(id, nameEn, nameVi, price, category, imgPath, ingredients))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return foodList
    }

    fun getFoodById(id: Int): FoodData {
        var food = FoodData("","", "", "", "", "", emptyList())
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM Food WHERE id = ?", arrayOf(id.toString()))
        if (cursor.moveToFirst()) {
            val nameEn = cursor.getString(cursor.getColumnIndexOrThrow("name_en"))
            val nameVi = cursor.getString(cursor.getColumnIndexOrThrow("name_vi"))
            val price = cursor.getString(cursor.getColumnIndexOrThrow("price"))
            val imgPath = cursor.getString(cursor.getColumnIndexOrThrow("img_path"))
            val category = cursor.getString(cursor.getColumnIndexOrThrow("category"))
            val ingredients = cursor.getString(cursor.getColumnIndexOrThrow("ingredients")).split(",").map { it.trim() }
            food = FoodData(id.toString(), nameEn, nameVi, price, category, imgPath, ingredients)
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

    // ===== INGREDIENT TABLE =====
    fun addIngredient(ingredient: IngredientData) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", ingredient.name)
            put("price", ingredient.price)
            put("category", ingredient.category)
            put("icon_img_path", ingredient.iconImgPath)
            put("layer_img_path", ingredient.layerImgPath)
        }
        db.insert("Ingredient", null, values)
        db.close()
    }

    fun getAllIngredients(): List<IngredientData> {
        val ingredientList = mutableListOf<IngredientData>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM Ingredient", null)
        if (cursor.moveToFirst()) {
            do {
                val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                val price = cursor.getString(cursor.getColumnIndexOrThrow("price"))
                val category = cursor.getString(cursor.getColumnIndexOrThrow("category"))
                val iconImgPath = cursor.getString(cursor.getColumnIndexOrThrow("icon_img_path"))
                val layerImgPath = cursor.getString(cursor.getColumnIndexOrThrow("layer_img_path"))
                ingredientList.add(IngredientData(name, price, category, iconImgPath, layerImgPath))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return ingredientList
    }

    fun getFoodByIngredient(ingredient: String): List<FoodData> {
        val foodList = mutableListOf<FoodData>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM Food WHERE ingredients LIKE ?", arrayOf("%$ingredient%"))
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("id")).toString()
                val nameEn = cursor.getString(cursor.getColumnIndexOrThrow("name_en"))
                val nameVi = cursor.getString(cursor.getColumnIndexOrThrow("name_vi"))
                val price = cursor.getString(cursor.getColumnIndexOrThrow("price"))
                val imgPath = cursor.getString(cursor.getColumnIndexOrThrow("img_path"))
                val category = cursor.getString(cursor.getColumnIndexOrThrow("category"))
                val ingredients = cursor.getString(cursor.getColumnIndexOrThrow("ingredients")).split(",").map { it.trim() }
                foodList.add(FoodData(id, nameEn, nameVi, price, category, imgPath, ingredients))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return foodList
    }

    fun deleteAllIngredients() {
        val db = writableDatabase
        // Kểm tra có bảng Ingredient không
        val cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='Ingredient'", null)
        if (cursor.count > 0) {
            db.execSQL("DELETE FROM Ingredient")
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
            }
            db.insert("Cart", null, values)
        }
        cursor.close()
        db.close()
    }

    fun addFoodToCartWithId(cartId: String, cartItem: CartData) {
        val db = writableDatabase

        // Kiểm tra xem món đã có trong giỏ chưa
        val ingredientsStr = cartItem.ingredients.sorted().joinToString(",")
        val query = """
            SELECT * FROM Cart
            WHERE id = ?
        """.trimIndent()

        val cursor = db.rawQuery(
            query,
            arrayOf(
                cartId
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
        }
        else
        {
            // Nếu không có, thêm mới vào giỏ
            val values = ContentValues().apply {
                put("id", cartId)
                put("food_id", cartItem.food_id)
                put("price", cartItem.price)
                put("ingredients", ingredientsStr)
                put("size", cartItem.size)
                put("crust", cartItem.crust)
                put("crust_base", cartItem.crustBase)
                put("quantity", cartItem.quantity)
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
                    "AND crust_base = ?",
            arrayOf(cartItem.food_id.toString(), cartItem.price.toString(), cartItem.ingredients.sorted().joinToString(","),
                cartItem.size, cartItem.crust, cartItem.crustBase)
        )
        var id = -1
        if (cursor.moveToFirst()) {
            id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
        }
        cursor.close()
        db.close()
        return id
    }

    fun getCartById(id: Int): CartData? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM Cart WHERE id = ?", arrayOf(id.toString()))
        var cartItem: CartData? = null

        if (cursor.moveToFirst()) {
            val foodId = cursor.getInt(cursor.getColumnIndexOrThrow("food_id"))
            val price = cursor.getInt(cursor.getColumnIndexOrThrow("price"))
            val ingredients = cursor.getString(cursor.getColumnIndexOrThrow("ingredients")).split(",").map { it.trim() }
            val size = cursor.getString(cursor.getColumnIndexOrThrow("size"))
            val crust = cursor.getString(cursor.getColumnIndexOrThrow("crust"))
            val crustBase = cursor.getString(cursor.getColumnIndexOrThrow("crust_base"))
            val quantity = cursor.getInt(cursor.getColumnIndexOrThrow("quantity"))

            cartItem = CartData(foodId, price, ingredients, size, crust, crustBase, quantity)
        }
        cursor.close()
        db.close()
        return cartItem
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
                val foodId = cursor.getString(cursor.getColumnIndexOrThrow("food_id")).toInt()
                val price = cursor.getString(cursor.getColumnIndexOrThrow("price")).toInt()
                val ingredients = cursor.getString(cursor.getColumnIndexOrThrow("ingredients")).split(",").map { it.trim() }
                val size = cursor.getString(cursor.getColumnIndexOrThrow("size"))
                val crust = cursor.getString(cursor.getColumnIndexOrThrow("crust"))
                val crustBase = cursor.getString(cursor.getColumnIndexOrThrow("crust_base"))
                val quantity = cursor.getInt(cursor.getColumnIndexOrThrow("quantity"))

                cartList.add(CartData(foodId, price, ingredients, size, crust, crustBase, quantity))
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
        db.delete("User", null, null)
        val values = ContentValues().apply {
            put("id", user.id)
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
            val id = cursor.getString(cursor.getColumnIndexOrThrow("id"))
            val email = cursor.getString(cursor.getColumnIndexOrThrow("email"))
            val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            val phone = cursor.getString(cursor.getColumnIndexOrThrow("phone"))
            val address = cursor.getString(cursor.getColumnIndexOrThrow("address"))
            val points = cursor.getInt(cursor.getColumnIndexOrThrow("points"))

            user = UserData(id, email, name, phone, address, points)
        }
        cursor.close()
        db.close()
        return user
    }

    fun deleteAllUser() {
        val db = writableDatabase
        // Kểm tra có bảng User không
        val cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='User'", null)
        if (cursor.count > 0) {
            db.execSQL("DELETE FROM User")
        }
        cursor.close()
        db.close()
    }

    // ===== RESTAURANT INFO TABLE =====
    fun addRestaurantInfo(name: String, address: String, phone: String, mail: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", name)
            put("address", address)
            put("phone", phone)
            put("mail", mail)
        }
        db.insert("RestaurantInfo", null, values)
        db.close()
    }

    fun getRestaurantInfo(): RestaurantData? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM RestaurantInfo LIMIT 1", null)
        var restaurantInfo: RestaurantData? = null

        if (cursor.moveToFirst()) {
            val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            val address = cursor.getString(cursor.getColumnIndexOrThrow("address"))
            val phone = cursor.getString(cursor.getColumnIndexOrThrow("phone"))
            val mail = cursor.getString(cursor.getColumnIndexOrThrow("mail"))

            restaurantInfo = RestaurantData(name, address, phone, mail)
        }
        cursor.close()
        db.close()
        return restaurantInfo
    }

    fun deleteAllRestaurantInfo() {
        val db = writableDatabase
        // Kểm tra có bảng RestaurantInfo không
        val cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='RestaurantInfo'", null)
        if (cursor.count > 0) {
            db.execSQL("DELETE FROM RestaurantInfo")
        }
        cursor.close()
        db.close()
    }

    // ===== ADDRESS TABLE =====
    private fun resetDefaultIfNeeded(db: SQLiteDatabase, isDefault: Boolean) {
        if (isDefault) {
            val resetValues = ContentValues().apply {
                put("isDefault", 0)
            }
            db.update("Address", resetValues, null, null)
        }
    }

    fun addAddress(address: AddressData) {
        val db = writableDatabase

        // Kiem tra address da ton tai chua
        val cursor = db.rawQuery(
            "SELECT * FROM Address WHERE name = ? AND phone = ? AND address = ?",
            arrayOf(address.name, address.phone, address.address)
        )

//        if (cursor.count > 0) {
//            // Dia chi da ton tai nhung isDefault co khac nhau khong
//            cursor.moveToFirst()
//            val isAddressDefault = if (address.isDefault) 1 else 0
//            if (cursor.getInt(cursor.getColumnIndexOrThrow("isDefault")) != isAddressDefault) {
//                // Dia chi da ton tai nhung isDefault khac nhau
//                val values = ContentValues().apply {
//                    put("isDefault", if (address.isDefault) 1 else 0)
//                }
//                db.update("Address", values, "name = ? AND phone = ? AND address = ?", arrayOf(address.name, address.phone, address.address))
//            }
//        }
//        else {
//            // Dia chi chua ton tai, them moi
//            val values = ContentValues().apply {
//                put("name", address.name)
//                put("phone", address.phone)
//                put("address", address.address)
//                put("isDefault", if (address.isDefault) 1 else 0)
//            }
//            db.insert("Address", null, values)
//        }

        if (cursor.moveToFirst()) {
            // Dia chi da ton tai
            resetDefaultIfNeeded(db, address.isDefault)

            // reset gia tri isDefault cua cac address khac
            val values = ContentValues().apply {
                put("isDefault", if (address.isDefault) 1 else 0)
            }
            db.update(
                "Address",
                values,
                "name = ? AND phone = ? AND address = ?",
                arrayOf(address.name, address.phone, address.address)
            )

        } else {
            // Dia chi chua ton tai

            resetDefaultIfNeeded(db, address.isDefault)

            // Thêm mới
            val values = ContentValues().apply {
                put("name", address.name)
                put("phone", address.phone)
                put("address", address.address)
                put("isDefault", if (address.isDefault) 1 else 0)
            }
            db.insert("Address", null, values)
        }

        cursor.close()
        db.close()
    }

    fun addAddressWithId(addressId: String, address: AddressData) {
        val db = writableDatabase

        resetDefaultIfNeeded(db, address.isDefault)

        val values = ContentValues().apply {
            put("id", addressId)
            put("name", address.name)
            put("phone", address.phone)
            put("address", address.address)
            put("isDefault", if (address.isDefault) 1 else 0)
        }
        db.insert("Address", null, values)
        db.close()
    }

    fun getAddressById(addressId: Int): AddressData? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM Address WHERE id = ?", arrayOf(addressId.toString()))
        var address: AddressData? = null

        if (cursor.moveToFirst()) {
            val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            val phone = cursor.getString(cursor.getColumnIndexOrThrow("phone"))
            val addressStr = cursor.getString(cursor.getColumnIndexOrThrow("address"))
            val isDefault = cursor.getInt(cursor.getColumnIndexOrThrow("isDefault")) == 1

            address = AddressData(name, phone, addressStr, isDefault)
        }
        cursor.close()
        db.close()
        return address
    }

    fun getAllAddresses(): List<AddressData> {
        val addressList = mutableListOf<AddressData>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM Address", null)

        if (cursor.moveToFirst()) {
            do {
                val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                val phone = cursor.getString(cursor.getColumnIndexOrThrow("phone"))
                val address = cursor.getString(cursor.getColumnIndexOrThrow("address"))
                val isDefault = cursor.getInt(cursor.getColumnIndexOrThrow("isDefault")) == 1

                addressList.add(AddressData(name, phone, address, isDefault))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return addressList
    }

    fun getDefaultAddress(): AddressData? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM Address WHERE isDefault = 1 LIMIT 1", null)
        var address: AddressData? = null

        if (cursor.moveToFirst()) {
            val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            val phone = cursor.getString(cursor.getColumnIndexOrThrow("phone"))
            val addressStr = cursor.getString(cursor.getColumnIndexOrThrow("address"))
            val isDefault = cursor.getInt(cursor.getColumnIndexOrThrow("isDefault")) == 1

            address = AddressData(name, phone, addressStr, isDefault)
        }
        cursor.close()
        db.close()
        return address
    }

    fun getAddressId(address: AddressData): Int {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT id FROM Address WHERE name = ? AND phone = ? AND address = ?",
            arrayOf(address.name, address.phone, address.address)
        )
        var id = -1
        if (cursor.moveToFirst()) {
            id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
        }
        cursor.close()
        db.close()
        return id
    }

    fun updateAddress(id: Int, address: AddressData) {
        val db = writableDatabase

        resetDefaultIfNeeded(db, address.isDefault)

        val values = ContentValues().apply {
            put("name", address.name)
            put("phone", address.phone)
            put("address", address.address)
            put("isDefault", if (address.isDefault) 1 else 0)
        }
        db.update("Address", values, "id = ?", arrayOf(id.toString()))
        db.close()
    }

    fun updateDefaultAddress(id: Int) {
        val db = writableDatabase
        val value_1 = ContentValues().apply {
            put("isDefault", 1)
        }

        val value_2 = ContentValues().apply {
            put("isDefault", 0)
        }

        // Đặt tất cả các địa chỉ khác thành không mặc định
        db.update("Address", value_2, "isDefault = 1", null)
        db.update("Address", value_1, "id = ?", arrayOf(id.toString()))
        db.close()
    }

    fun deleteAddress(id: Int) {
        val db = writableDatabase
        // Kểm tra có bảng Address không
        val cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='Address'", null)
        if (cursor.count > 0) {
            db.execSQL("DELETE FROM Address WHERE id = ?", arrayOf(id.toString()))
        }
        cursor.close()
        db.close()
    }

    fun deleteAllAddress() {
        val db = writableDatabase
        // Kểm tra có bảng Address không
        val cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='Address'", null)
        if (cursor.count > 0) {
            db.execSQL("DELETE FROM Address")
        }
        cursor.close()
        db.close()
    }


}