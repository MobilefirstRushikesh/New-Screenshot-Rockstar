package com.screenshot.rockstar.interfaces


interface ChangeFragmentListener {

    fun addFragment(fragmentTag:String, addToBackStack:Boolean)
    fun replaceFragment(fragmentTag: String, addToBackStack: Boolean)
    fun popBackStack()
    fun removeAndAddFragment(removeFragmentTag: String,addFragmentTag: String, addToBackStack: Boolean, popCurrentFragFromBackStack:Boolean)
}