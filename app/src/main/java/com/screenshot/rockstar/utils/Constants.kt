package com.screenshot.rockstar.utils

class Constants {

    companion object{

        //Subscription constants
        const val MONTHLY_SUB = "jaadoo_monthly"
        const val YEARLY_SUB = "jaadoo_yearly"
        const val BASIC_SUB = "jaadoo_basic"

        const val EXTRA_PATH = "path"
        const val EXTRA_TEXT = "text"
        const val EXTRA_POSITION = "position"
        const val EXTRA_TAG_NAME = "tag"
        const val EXTRA_TAG_PHOTO_COUNT = "count"
        const val INTENT_SERVICE_PATH = "ServicePath"
        const val FILE_CREATION_TAG = "fileCreate"
        const val FILTER_TAG = "filterTag"
        const val INSERT_FROM_INIT = "insert_init"
        const val INSERT_FROM_REFRESH = "insert_refresh"
        const val SORT_PREFERENCE_DATE_ASC = "sortByDateAsc"
        const val SORT_PREFERENCE_DATE_DESC = "sortByDateDesc"

        //Ads unit constants
        const val ITEMS_PER_AD = 50
        const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-5339372327780825/7139401414"
        const val BANNER_AD_UNIT_ID = "ca-app-pub-5339372327780825/9165060847"
        const val BANNER_AD_UNIT_ID_TEST = "ca-app-pub-3940256099942544/6300978111"
        const val INTERSTITIAL_AD_UNIT_ID_TEST = "ca-app-pub-3940256099942544/1033173712"

        //Request code constants
        const val REQUEST_CODE_DELETE_IMAGE = 2
        const val REQUEST_CODE_CHANGE_DIRECTORY = 1
        const val REQUEST_CODE_TAG_ADDED = 3
        const val REQUEST_CODE_TAG_DELETED = 4
        const val REQUEST_CODE_IMAGE_DETAIL = 5
        const val REQUEST_CODE_IMAGE_SHARE = 6
        const val REQUEST_CODE_STORAGE_PERMISSION = 7
        const val REQUEST_CODE_ALL_FILE_ACCESS_PERMISSION = 8


        //Session pref
        const val PREF_NAME = "JaadooSession"
        const val PRIVATE_MODE = 0
        const val HAS_SUBSCRIPTION_PREF = "hasSubscription"
        const val PREV_SUBSCRIPTION_PREF = "PrevSubscription"
        const val DIRECTORY_PREF = "directory_preference"
        const val SORT_PREF = "sort_preference"
        const val DEFAULT_DIRECTORY = "default_directory"
        const val FIRST_TIME = "is_user_first_time"


        //Action Processes
        const val ACTION_DELETE = "actionDelete"
        const val ACTION_TAG = "actionTag"
        const val ACTION_SHARE = "actionShare"
        const val ACTION_INSERT = "actionInsert"
        const val ACTION_UPDATE = "actionUpdate"

        //Image Model
        const val IMAGE_PATH = "ImageModelPath"
        const val IMAGE_TEXT = "ImageModelText"
        const val IMAGE_CREATION_TIME = "ImageModelCreationTime"
        const val IMAGE_HAS_TAG = "ImageModelHasTag"
        const val IMAGE_PATH_FILE = "ImageModelPathFile"
        const val SINGLE_SELECTION_MODE = "singleSelectionMode"
        const val MULTI_SELECTION_MODE = "multiSelectionMode"

        //URL
        const val URL_TERMS_CONDITION = "https://www.jaadoo.ai/privacy-policy.html"

        //KEY
        const val PLAY_CONSOLE_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAlO94XL1kVVGe2bcQHEwTAJ6uajkOTsW8WRaNVMeibea7VCz9XYL0mG+r11YwfB/lP/g4w1txHiz+/Ctqo73S4BjwTRWYx/yEqKEAe2Mq9cV0QitCC9B3RPJTu8l6xLdaBMhOx6e4LvADtiyA0Q9ULXvl62oWICTdv8Smbo53QHUupxh211kCmwVY/CRZABig3c82nnap2HCLeM16vVvYnt5P5JfvA+1to2dNfmzuCUG6Aji4coq/0N3fTi8tvcxwvmCSTaJu4TNlM1/vY3WLSZU38yzrh2ehOVUzvIUexGXh3m7+nIvKltT5o/SEnBPl6MWfwcAy2lSTrkeNBj0QdwIDAQAB"
        const val REVENUE_CAT_PUBLIC_KEY = "HzmOMrAZtTLTbdKEvzXhxQkSiYcUWbwy"
        const val REVENUE_CAT_PRO_PUBLIC_KEY = "qSzTsICFFLCsmcevxZWQYoVfxkpkYIeX"

        //Activity amd fragments TAG
        const val MAIN_ACTIVITY_TAG = "MainActivities"
        const val SEARCH_FRAG_TAG = "SearchFragment"
        const val IMAGE_DETAIL_FRAG_TAG = "ImageDetailFragment"
        const val BILLING_FRAG_TAG = "billingFragment"
        const val INIT_SETUP_FRAG_TAG = "initFragment"
        const val SPLASH_SCREEN_FRAG_TAG = "splashScreenFragment"
        const val ON_BOARD_FRAG_TAG = "onBoardingFragment"
        const val DELETE_CONFIRM_DIALOG_TAG = "deleteConfirmDialog"
        const val PROCESS_DIALOG_TAG = "processDialog"
        const val TAG_BOTTOM_DIALOG_TAG = "tagBottomSheetDialog"
        const val MAIN_OPTION_MENU_DIALOG = "mainOptionDialog"
        const val VIEW_HIDDEN_SHOTS_FRAG_TAG = "hiddenShotsFragment"

        //Revenue Cat
        const val REVENUE_ENTITLEMENT_ID_PRO = "jaadoo_pro"


        const val PURCHASE_VERIFICATION_FAILED = 111

        //AppFlyer Constant
        const val SUBSCRIPTION_TYPE = "subscription type"
        const val USER_CANCELLED = "user cancelled"
        const val SCREEN_EVENT = "screen event"
    }
}