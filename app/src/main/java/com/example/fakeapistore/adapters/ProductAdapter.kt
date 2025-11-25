package com.example.fakeapistore.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.RoundedCornersTransformation
import com.example.groceryapp.R
import com.example.fakeapistore.models.Product
import com.google.android.material.button.MaterialButton

class ProductAdapter(
    private var products: List<Product>,
    private val onProductClick: (Product) -> Unit,
    private val onAddClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val productImage: ImageView = itemView.findViewById(R.id.productImage)
        private val productTitle: TextView = itemView.findViewById(R.id.productTitle)
        private val productWeight: TextView = itemView.findViewById(R.id.productWeight)
        private val productPrice: TextView = itemView.findViewById(R.id.productPrice)
        private val addButton: MaterialButton = itemView.findViewById(R.id.addButton)  // Changed: favoriteButton -> addButton, and made it private

        fun bind(product: Product) {
            productTitle.text = product.title
            productWeight.text = product.category
            productPrice.text = "$${String.format("%.2f", product.price)}"

            productImage.load(product.image) {
                crossfade(true)
                placeholder(R.drawable.placeholder_image)
                error(R.drawable.placeholder_image)
                transformations(RoundedCornersTransformation(16f))
            }

            itemView.setOnClickListener {
                onProductClick(product)
            }

            addButton.setOnClickListener {  // Now this matches the property name
                onAddClick(product)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount() = products.size

    fun updateProducts(newProducts: List<Product>) {
        products = newProducts
        notifyDataSetChanged()
    }
}