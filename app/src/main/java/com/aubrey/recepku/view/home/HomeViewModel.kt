package com.aubrey.recepku.view.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.aubrey.recepku.data.model.recommended.Recommended
import com.aubrey.recepku.data.repository.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import com.aubrey.recepku.data.common.Result
import com.aubrey.recepku.data.repository.UserRepository
import com.aubrey.recepku.data.response.DataItem
import com.aubrey.recepku.data.response.RecipeResponse
import kotlinx.coroutines.flow.Flow

class HomeViewModel(private val recipeRepository: RecipeRepository, private val repository: UserRepository) : ViewModel() {


    private val recipeList = MutableLiveData<Result<RecipeResponse>>()
    val recipeData: LiveData<Result<RecipeResponse>> = recipeList

    private val _uiState1: MutableStateFlow<Result<List<Recommended>>> = MutableStateFlow(Result.Loading)
    val uiState1: StateFlow<Result<List<Recommended>>>
        get() = _uiState1

    fun searchRecipe(search: String) = recipeRepository.searchRecipe(search)

    fun getRecipes() {
        viewModelScope.launch {
            val recipeResponse = recipeRepository.getAllRecipes()
            recipeResponse.asFlow().collect {
                recipeList.value = it
            }
        }
    }


    fun getAllRecommendedRecipes() {
        viewModelScope.launch {
            recipeRepository.getAllRecommendedRecipes()
                .catch {
                    _uiState1.value = Result.Error(it.message.toString())
                }
                .collect { recommendedRecipes ->
                    _uiState1.value = Result.Success(recommendedRecipes)
                }
        }
    }

//    fun searchRecipe(query: String) {
//        viewModelScope.launch {
//            recipeRepository.searchRecipe(query)
//                .catch {throwable ->
//                    _uiState.value = Result.Error(throwable.message.toString())
//                }
//                .collect { filteredRecipes ->
//                    _uiState.value = Result.Success(RecipeResponse(filteredRecipes))
//                }
//        }
//    }
}