package io.github.binarystation.grocerynotes

/** One row of the shopping list. The id never changes, even when the row moves up the list. */
data class GroceryItem(val id: Int, val name: String)

fun sampleGroceries(): List<GroceryItem> = listOf(
    GroceryItem(1, "Rice"),
    GroceryItem(2, "Oil"),
    GroceryItem(3, "Milk"),
    GroceryItem(4, "Eggs"),
    GroceryItem(5, "Tomatoes"),
    GroceryItem(6, "Bread"),
)
