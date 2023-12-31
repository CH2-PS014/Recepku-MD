package com.aubrey.recepku.view.home

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import com.aubrey.recepku.view.search.SearchActivity
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.aubrey.recepku.databinding.FragmentHomeBinding
import com.aubrey.recepku.view.ViewModelFactory
import com.aubrey.recepku.view.adapter.RecipeAdapter
import com.denzcoskun.imageslider.ImageSlider
import com.denzcoskun.imageslider.models.SlideModel
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.aubrey.recepku.R
import com.aubrey.recepku.view.adapter.RecommendedRecipeAdapter
import com.aubrey.recepku.data.common.Result
import com.aubrey.recepku.data.model.recommended.Recommended
import com.aubrey.recepku.view.SettingViewModel
import com.aubrey.recepku.data.response.DataItem
import com.aubrey.recepku.view.adapter.IngredientsAdapter
import com.aubrey.recepku.view.adapter.LowCalIngredientsAdapter
import com.aubrey.recepku.view.adapter.LowCalStepsAdapter
import com.aubrey.recepku.view.adapter.StepsAdapter
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch
import androidx.cardview.widget.CardView
import com.aubrey.recepku.MainActivity
import com.aubrey.recepku.data.database.FavoriteRecipe
import com.aubrey.recepku.data.userpref.UserPreferences
import com.aubrey.recepku.data.userpref.dataStore
import com.aubrey.recepku.view.edituser.EditUserActivity
import com.aubrey.recepku.view.login.LoginActivity
import com.aubrey.recepku.view.setting.SettingActivity
import com.aubrey.recepku.view.welcome_page.WelcomeActivity
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.flow.firstOrNull

interface RecipeClickListener {
    fun onRecipeClicked(recipe: DataItem)
}

interface RecommendedRecipeClickListener {
    fun onRecipeClicked(recipe: Recommended)
}

class HomeFragment : Fragment(), RecipeClickListener, RecommendedRecipeClickListener {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var imageSlider: ImageSlider
    private lateinit var recommendedAdapter: RecommendedRecipeAdapter



    private val viewModel: HomeViewModel by viewModels {
        ViewModelFactory.getInstance(requireActivity().application)
    }



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvRecipe.layoutManager = layoutManager
        imageSlider = binding.slider


        val recommendedLayoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        binding.rvRecommended.layoutManager = recommendedLayoutManager

        recommendedAdapter = RecommendedRecipeAdapter(this)
        binding.rvRecommended.adapter = recommendedAdapter

        setupImageSlider()
        getRecipes()
        getRecommendedRecipes()
        searchBar()
        setDarkMode()
        playAnimation()
    }

    private fun setupImageSlider() {
        val imageList = listOf(
            SlideModel("https://www.herofincorp.com/public/admin_assets/upload/blog/64b91a06ab1c8_food%20business%20ideas.webp"),
            SlideModel("https://wallpapers.com/images/high/food-4k-3gsi5u6kjma5zkj0.webp"),
            SlideModel("https://c4.wallpaperflare.com/wallpaper/443/638/469/japanese-cuisine-food-japanese-food-sashimi-wallpaper-preview.jpg")
        )
        imageSlider.setImageList(imageList)
    }

    private fun getRecipes() {
        viewModel.recipeData.observe(viewLifecycleOwner) { result ->
            binding.apply {
                when (result) {
                    is Result.Loading -> {
                        progressBar.visibility = View.VISIBLE
                        Log.d("Loading", "Loading")
                    }

                    is Result.Success -> {
                        progressBar.visibility = View.GONE
                        val recipe = result.data.data
                        if (recipe != null) {
                            setRecipe(recipe)
                            Log.d("Success", "Recipe Fetched")
                        }
                    }
                    is Result.Error -> {
                        val errorMessage = result.error
                        Log.d("Failed", "Recipe not fetched. Error message: $errorMessage")
                    }
                }
            }
        }
    }

    private fun setRecipe(recipe: List<DataItem?>?) {
        binding.apply {
            rvRecipe.adapter = RecipeAdapter(recipe, this@HomeFragment)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.getRecipes()
    }


    private fun getRecommendedRecipes() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState1.collect { uiState1 ->
                when (uiState1) {
                    is Result.Loading -> {
                        viewModel.getAllRecommendedRecipes()
                        Log.d("Recommended HomeScreen", "Loading: Sabar")
                    }

                    is Result.Success -> {
                        recommendedAdapter.submitList(uiState1.data)
                        Log.d("Recommended HomeScreen", "Success: Nih Resep")
                    }

                    is Result.Error -> {
                        // Tindakan yang diambil saat terjadi kesalahan
                        Log.d("Recommended HomeScreen", "Error: Kok gabisa si")
                    }

                    else -> {}
                }
            }
        }
    }
    //Belum bisa


    override fun onRecipeClicked(recipe: DataItem) {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        val inflater = LayoutInflater.from(requireContext())
        val dialogView = inflater.inflate(R.layout.item_card_detail, null) as CardView

//      Adapter

        // Adapter
        val stepsAdapter = StepsAdapter(recipe.steps ?: emptyList())
        val ingredientsAdapter = IngredientsAdapter(recipe.ingredients ?: emptyList())
        val lowCalStepsAdapter = LowCalStepsAdapter(recipe.healthySteps ?: emptyList())
        val lowCalIngAdapter = LowCalIngredientsAdapter(recipe.healthyIngredients ?: emptyList())

//        ui
        val ivRecipe = dialogView.findViewById<ImageView>(R.id.foodImage)
        val tvRecipeName = dialogView.findViewById<TextView>(R.id.tvRecipeName)
        val tvRecipeDescription = dialogView.findViewById<TextView>(R.id.tv_description)
        val tvCalories = dialogView.findViewById<TextView>(R.id.tv_calories_value)
        val rvIngredients = dialogView.findViewById<RecyclerView>(R.id.rv_ingredients)
        val rvSteps = dialogView.findViewById<RecyclerView>(R.id.rv_steps)
        val backBtn = dialogView.findViewById<ImageButton>(R.id.btn_back_detail)
        val favBtn = dialogView.findViewById<ImageButton>(R.id.btn_favorite_detail)
        val lowcalBtn = dialogView.findViewById<ImageButton>(R.id.btn_lowcal)

//        condition
        var isLowcal = false
        var isFavorite = recipe.isFavorite == false

//        setup
        Glide.with(ivRecipe)
            .load(recipe.photoUrl)
            .into(ivRecipe)
        tvRecipeName.text = recipe.title
        tvRecipeDescription.text = recipe.description

        val layoutManager = GridLayoutManager(requireContext(), 2)
        val stepsLayoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        rvIngredients.layoutManager = layoutManager
        rvIngredients.adapter = ingredientsAdapter

        rvSteps.adapter = stepsAdapter
        rvSteps.layoutManager = stepsLayoutManager

        tvCalories.text = recipe.calories.toString()



        val alertDialog = dialogBuilder.setView(dialogView).create()

        lowcalBtn.setOnClickListener {
            if (isLowcal) {
                lowcalBtn.setImageResource(R.drawable.ic_food)
                isLowcal = false
                rvIngredients.adapter = ingredientsAdapter
                tvCalories.text = recipe.calories.toString()
                rvSteps.adapter = stepsAdapter

            } else {
                lowcalBtn.setImageResource(R.drawable.ic_food_healthy)
                isLowcal = true
                rvIngredients.adapter = lowCalIngAdapter
                tvCalories.text = recipe.healthyCalories.toString()
                rvSteps.adapter = lowCalStepsAdapter
            }
        }

        backBtn.setOnClickListener {
            alertDialog.dismiss()
        }

        favBtn.setOnClickListener {
            if (isFavorite == false) {
                favBtn.setImageResource(R.drawable.ic_favorite_border)
                isFavorite = true
                viewModel.delete(
                    FavoriteRecipe(
                        recipe.id,
                        recipe.title,
                        recipe.description,
                        recipe.photoUrl,
                        recipe.ingredients,
                        recipe.steps,
                        recipe.healthyIngredients,
                        recipe.healthySteps,
                        recipe.calories,
                        recipe.healthyCalories,
                    )
                )
            } else {
                favBtn.setImageResource(R.drawable.ic_favorite_fill)
                isFavorite = false
                viewModel.insert(
                    FavoriteRecipe(
                        recipe.id,
                        recipe.title,
                        recipe.description,
                        recipe.photoUrl,
                        recipe.ingredients,
                        recipe.steps,
                        recipe.healthyIngredients,
                        recipe.healthySteps,
                        recipe.calories,
                        recipe.healthyCalories,
                    )
                )
            }
        }


        alertDialog.show()
    }

    override fun onRecipeClicked(recipe: Recommended) {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        val inflater = LayoutInflater.from(requireContext())
        val dialogView = inflater.inflate(R.layout.item_card_detail, null)


        val ingredientsAdapter = recipe.recommended.ingredients?.let { IngredientsAdapter(it) }
        val stepsAdapter = recipe.recommended.steps?.let { StepsAdapter(it) }
        val lowCalIngAdapter = recipe.recommended.healthyIngredients?.let { LowCalIngredientsAdapter(it) }
        val lowCalStepsAdapter = recipe.recommended.healthySteps?.let { LowCalStepsAdapter(it) }

        val backBtn = dialogView.findViewById<ImageButton>(R.id.btn_back_detail)
        val favBtn = dialogView.findViewById<ImageButton>(R.id.btn_favorite_detail)
        val lowcalBtn = dialogView.findViewById<ImageButton>(R.id.btn_lowcal)

        val ivRecipe = dialogView.findViewById<ImageView>(R.id.foodImage)
        val tvRecipeName = dialogView.findViewById<TextView>(R.id.tvRecipeName)
        val tvRecipeDescription = dialogView.findViewById<TextView>(R.id.tv_description)
        val rvIngredients = dialogView.findViewById<RecyclerView>(R.id.rv_ingredients)
        val rvSteps = dialogView.findViewById<RecyclerView>(R.id.rv_steps)
        val tvCalories = dialogView.findViewById<TextView>(R.id.tv_calories_value)

        //        condition
        var isLowcal = false
        var isFavorite = recipe.recommended.isFavorite

        //        setup
        Glide.with(ivRecipe)
            .load(recipe.recommended.photoUrl)
            .into(ivRecipe)
        tvRecipeName.text = recipe.recommended.title
        tvRecipeDescription.text = recipe.recommended.description

        val layoutManager = GridLayoutManager(requireContext(), 2)
        val stepsLayoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        rvIngredients.layoutManager = layoutManager
        rvIngredients.adapter = ingredientsAdapter

        rvSteps.adapter = stepsAdapter
        rvSteps.layoutManager = stepsLayoutManager

        tvCalories.text = recipe.recommended.calories.toString()



        val alertDialog = dialogBuilder.setView(dialogView).create()

        lowcalBtn.setOnClickListener {
            if (isLowcal) {
                lowcalBtn.setImageResource(R.drawable.ic_food)
                isLowcal = false
                rvIngredients.adapter = ingredientsAdapter
                tvCalories.text = recipe.recommended.calories.toString()
                rvSteps.adapter = stepsAdapter

            } else {
                lowcalBtn.setImageResource(R.drawable.ic_food_healthy)
                isLowcal = true
                rvIngredients.adapter = lowCalIngAdapter
                tvCalories.text = recipe.recommended.healthyCalories.toString()
                rvSteps.adapter = lowCalStepsAdapter
            }
        }

        backBtn.setOnClickListener {
            alertDialog.dismiss()
        }

        favBtn.setOnClickListener {
            if (isFavorite) {
                favBtn.setImageResource(R.drawable.ic_favorite_border)
                isFavorite = false
                viewModel.delete(
                    FavoriteRecipe(
                        recipe.recommended.id,
                        recipe.recommended.title,
                        recipe.recommended.description,
                        recipe.recommended.photoUrl,
                        recipe.recommended.ingredients,
                        recipe.recommended.steps,
                        recipe.recommended.healthyIngredients,
                        recipe.recommended.healthySteps,
                        recipe.recommended.calories,
                        recipe.recommended.healthyCalories,
                    )
                )
            } else {
                favBtn.setImageResource(R.drawable.ic_favorite_fill)
                isFavorite = true
                viewModel.insert(
                    FavoriteRecipe(
                        recipe.recommended.id,
                        recipe.recommended.title,
                        recipe.recommended.description,
                        recipe.recommended.photoUrl,
                        recipe.recommended.ingredients,
                        recipe.recommended.steps,
                        recipe.recommended.healthyIngredients,
                        recipe.recommended.healthySteps,
                        recipe.recommended.calories,
                        recipe.recommended.healthyCalories,
                    )
                )
            }
        }




        alertDialog.show()
    }

    private fun searchBar() {
        with(binding) {
            searchView.setupWithSearchBar(searchBar)
            searchView.editText.setOnEditorActionListener { view, actionId, event ->
                val query = searchView.text.toString()
                val intent = Intent(requireContext(), SearchActivity::class.java)
                intent.putExtra("searchQuery", "$query")
                startActivity(intent)
                searchBar.setText(searchView.text)
                searchView.hide()
                viewModel.search(searchView.text.toString())
                false
            }
            searchBar.inflateMenu(R.menu.option_menu)
            searchBar.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.profile_icon -> {
                        showProfile()
                        true
                    }

                    else -> false
                }
            }
        }
    }

    @SuppressLint("MissingInflatedId")
    private fun showProfile() {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        val inflater = LayoutInflater.from(requireContext())
        val dialogView = inflater.inflate(R.layout.card_profile, null)
        val tvName = dialogView.findViewById<TextView>(R.id.tv_name)
        val tvEmail = dialogView.findViewById<TextView>(R.id.tv_email)
        val settingBtn = dialogView.findViewById<ImageButton>(R.id.btn_setting)
        val backBtn = dialogView.findViewById<ImageButton>(R.id.btn_back_detail)
        val logoutBtn = dialogView.findViewById<CardView>(R.id.cardLogout)
        val setAccount = dialogView.findViewById<CardView>(R.id.cardAccount)
        val deleteAccount = dialogView.findViewById<CardView>(R.id.cardDeleteAccount)
        viewModel.saveUser()
        deleteAccount.setOnClickListener {
               AlertDialog.Builder(requireContext()).apply {
                   setTitle("Hapus Akun")
                   setMessage("Anda Yakin Ingin Menghapus Akun?")
                   setPositiveButton("Lanjut"){_,_ ->
                       viewModel.deleteUser().observe(viewLifecycleOwner) {
                           when (it) {
                               is Result.Loading -> {
                                   Log.d("Home Fragment", "Sabar cuy")
                               }
                               is Result.Success -> {
                                  val intent = Intent(requireContext(),MainActivity::class.java)
                                   intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                   startActivity(intent)
                               }
                               is Result.Error -> {
                                   val error = it.error
                                   Log.d("Home Fragment", "Error $error" )
                                   Log.e("Home Fragment", "Error cuy")
                               }
                           }
                       }
                   }
                   setNegativeButton("Tidak"){_,_->
                       showProfile()
                   }
                   create().show()
               }
        }

//        setAccount.setOnClickListener {
//            val intent = Intent(requireContext(), EditUserActivity::class.java)
//            startActivity(intent)
//        }

        readUserData { username, email ->
            tvName.text = username
            tvEmail.text = email
        }

        val alertDialog = dialogBuilder.setView(dialogView).create()
        settingBtn.setOnClickListener {
            val intent = Intent(requireContext(), SettingActivity::class.java)
            startActivity(intent)
            alertDialog.dismiss()
        }

        backBtn.setOnClickListener {
            alertDialog.dismiss()
        }

        logoutBtn.setOnClickListener {
            val preference = UserPreferences.getInstance(requireActivity().application.dataStore)
            lifecycleScope.launch {
                preference.deleteCookies()
                Log.d("Deleted", "Cookies Deleted")
            }

            val intent = Intent(requireContext(), WelcomeActivity::class.java)
            startActivity(intent)
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    private fun readUserData(callback: (username: String, email: String) -> Unit) {
        val dataStore = requireContext().dataStore

        lifecycleScope.launch {
            val userData = dataStore.data.firstOrNull()

            val username = userData?.get(UserPreferences.NAME_KEY) ?: ""
            val email = userData?.get(UserPreferences.EMAIL_KEY) ?: ""

            callback(username, email)
        }
    }

    private fun setDarkMode(){
        val viewModel by viewModels<SettingViewModel> {
            ViewModelFactory.getInstance(requireActivity().application)
        }
        viewModel.getThemeSetting().observe(viewLifecycleOwner){
            if (it){
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }else{
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }

    private fun playAnimation(){
        val slide = ObjectAnimator.ofFloat(binding.slider, "alpha", 0f, 1f).setDuration(800)
        val recommended = ObjectAnimator.ofFloat(binding.tvRecommendedRecipe, "alpha", 0f, 1f).setDuration(500)
        val rvRecommended = ObjectAnimator.ofFloat(binding.rvRecommended, "alpha", 0f, 1f).setDuration(500)
        val recipe = ObjectAnimator.ofFloat(binding.tvRecipe, "alpha", 0f, 1f).setDuration(500)
        val rvRecipe = ObjectAnimator.ofFloat(binding.rvRecipe, "alpha", 0f, 1f).setDuration(500)

        AnimatorSet().apply {
            playSequentially(slide, recommended, rvRecommended, recipe, rvRecipe)
            start()
        }
    }
}