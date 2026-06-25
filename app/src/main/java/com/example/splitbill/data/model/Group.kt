package com.example.splitbill.data.model

import com.google.firebase.firestore.DocumentId

data class Group(
    @DocumentId
    val groupId: String = "",
    val groupName: String = "",
    val joinCode: String = "",
    val createdBy: String = "",
    val members: List<String> = emptyList()
)
