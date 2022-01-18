package com.sigma.myjoysticktest.pojo

import com.google.gson.annotations.SerializedName

class PayloadPublishReq(
    @field:SerializedName("leftAngle")
    var leftAngle: String? = null,

    @field:SerializedName("leftStrength")
    var leftStrength: String? = null,

    @field:SerializedName("rigtAngle")
    var rigtAngle: String? = null,

    @field:SerializedName("rightStrength")
    var rightStrength: String? = null,

    @field:SerializedName("rightCoordinate")
    var rightCoordinate: String? = null
)