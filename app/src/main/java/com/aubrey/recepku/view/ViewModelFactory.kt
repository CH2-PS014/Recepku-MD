package com.aubrey.recepku.view

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.aubrey.recepku.data.Injection
import com.aubrey.recepku.data.repository.RecipeRepository
import com.aubrey.recepku.data.repository.UserRepository
import com.aubrey.recepku.view.register.RegisterActivity
import com.aubrey.recepku.view.register.RegisterViewModel
import com.aubrey.recepku.view.viewmodels.HomeViewModel

class ViewModelFactory private constructor(private val repository: UserRepository,
                                           private val recipeRepository: RecipeRepository
):
    ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when{
            modelClass.isAssignableFrom(RegisterViewModel::class.java) ->{
                RegisterViewModel(repository) as T
            }
            modelClass.isAssignableFrom(HomeViewModel::class.java) ->{
                HomeViewModel(recipeRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class:" + modelClass.name)
        }
    }

    companion object {
        private var INSTANCE: ViewModelFactory? = null

        fun clearInstance() {
            UserRepository.clearInstance()
            INSTANCE = null
        }

        fun getInstance(context: Context): ViewModelFactory {
            if (INSTANCE == null) {
                synchronized(ViewModelFactory::class.java) {
                    INSTANCE = ViewModelFactory(
                        Injection.provideRepository(context),
                        Injection.provideRecipeRepository(context)
                    )
                }
            }
            return INSTANCE as ViewModelFactory
        }
    }
}