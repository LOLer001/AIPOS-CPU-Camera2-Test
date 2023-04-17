package cn.com.techvision.aipos.entity.ui

import com.techvision.aipos.R

class PosEntity(
    var ids: Int = R.drawable.no_pic,
    var uiName: String = "unknown",
    var name: String = "unknown",
    var convertName: String = "unknown",
    var probability: String = "50",
    var price: Int = 50
)