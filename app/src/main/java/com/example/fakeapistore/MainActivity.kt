package com.example.fakeapistore

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.RoundedCornersTransformation
import com.example.fakeapistore.adapters.ProductAdapter
import com.example.fakeapistore.models.Product
import com.example.fakeapistore.network.RetrofitClient
import com.example.groceryapp.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var categorySpinner: Spinner
    private lateinit var productsRecyclerView: RecyclerView
    private lateinit var loadingProgress: ProgressBar
    private lateinit var emptyStateText: TextView
    private lateinit var sectionTitle: TextView

    private lateinit var productAdapter: ProductAdapter
    private val categories = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeViews()
        setupRecyclerView()
        loadCategories()
    }

    private fun initializeViews() {
        categorySpinner = findViewById(R.id.categorySpinner)
        productsRecyclerView = findViewById(R.id.productsRecyclerView)
        loadingProgress = findViewById(R.id.loadingProgress)
        emptyStateText = findViewById(R.id.emptyStateText)
        sectionTitle = findViewById(R.id.sectionTitle)
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter(
            products = emptyList(),
            onProductClick = { product ->
                showProductDialog(product)
            },
            onAddClick = { product ->
                Toast.makeText(
                    this,
                    "${product.title} added to cart!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )

        val spanCount = 2
        val spacing = dpToPx(8) // 8dp spacing between items

        productsRecyclerView.apply {
            layoutManager = GridLayoutManager(this@MainActivity, spanCount)
            adapter = productAdapter
            setHasFixedSize(true)
            addItemDecoration(GridSpacingItemDecoration(spanCount, spacing, false))
        }
    }

    // Add this helper function at the bottom of MainActivity class
    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    private fun loadCategories() {
        showLoading(true)

        RetrofitClient.apiService.getCategories().enqueue(object : Callback<List<String>> {
            override fun onResponse(call: Call<List<String>>, response: Response<List<String>>) {
                if (response.isSuccessful) {
                    response.body()?.let { categoriesList ->
                        categories.clear()
                        categories.add("All Products")
                        categories.addAll(categoriesList)
                        setupSpinner()
                        // Load all products initially
                        loadProducts("all")
                    }
                } else {
                    showError("Failed to load categories")
                }
            }

            override fun onFailure(call: Call<List<String>>, t: Throwable) {
                showError("Network error: ${t.message}")
            }
        })
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            categories
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        categorySpinner.adapter = adapter

        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedCategory = categories[position]
                updateSectionTitle(selectedCategory)

                if (position == 0) {
                    loadProducts("all")
                } else {
                    loadProducts(selectedCategory)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    private fun updateSectionTitle(category: String) {
        sectionTitle.text = when (category) {
            "All Products" -> "Exclusive Offer"
            else -> category.replaceFirstChar { it.uppercase() }
        }
    }

    private fun loadProducts(category: String) {
        showLoading(true)

        val call = if (category == "all") {
            RetrofitClient.apiService.getAllProducts()
        } else {
            RetrofitClient.apiService.getProductsByCategory(category)
        }

        call.enqueue(object : Callback<List<Product>> {
            override fun onResponse(call: Call<List<Product>>, response: Response<List<Product>>) {
                showLoading(false)

                if (response.isSuccessful) {
                    response.body()?.let { products ->
                        if (products.isEmpty()) {
                            showEmptyState(true)
                        } else {
                            showEmptyState(false)
                            productAdapter.updateProducts(products)
                        }
                    }
                } else {
                    showError("Failed to load products")
                }
            }

            override fun onFailure(call: Call<List<Product>>, t: Throwable) {
                showLoading(false)
                showError("Network error: ${t.message}")
            }
        })
    }

    private fun showProductDialog(product: Product) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_product_detail)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )

        // Initialize dialog views
        val closeButton = dialog.findViewById<ImageButton>(R.id.closeButton)
        val productImage = dialog.findViewById<ImageView>(R.id.dialogProductImage)
        val productTitle = dialog.findViewById<TextView>(R.id.dialogProductTitle)
        val productCategory = dialog.findViewById<Chip>(R.id.dialogProductCategory)
        val productPrice = dialog.findViewById<TextView>(R.id.dialogProductPrice)
        val productDescription = dialog.findViewById<TextView>(R.id.dialogProductDescription)
        val addToCartButton = dialog.findViewById<MaterialButton>(R.id.addToCartButton)

        // Populate dialog with product data
        productImage.load(product.image) {
            crossfade(true)
            placeholder(R.drawable.placeholder_image)
            error(R.drawable.placeholder_image)
            transformations(RoundedCornersTransformation(16f))
        }

        productTitle.text = product.title
        productCategory.text = product.category
        productPrice.text = "$${String.format("%.2f", product.price)}"
        productDescription.text = product.description

        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        addToCartButton.setOnClickListener {
            Toast.makeText(
                this,
                "${product.title} added to cart!",
                Toast.LENGTH_SHORT
            ).show()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showLoading(show: Boolean) {
        loadingProgress.visibility = if (show) View.VISIBLE else View.GONE
        productsRecyclerView.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun showEmptyState(show: Boolean) {
        emptyStateText.visibility = if (show) View.VISIBLE else View.GONE
        productsRecyclerView.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun showError(message: String) {
        showLoading(false)
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}