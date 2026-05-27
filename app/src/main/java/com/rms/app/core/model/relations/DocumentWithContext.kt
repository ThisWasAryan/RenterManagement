package com.rms.app.core.model.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.rms.app.core.model.entities.Document
import com.rms.app.core.model.entities.Tenant

data class DocumentWithContext(
    @Embedded val document: Document,
    @Relation(
        parentColumn = "tenantId",
        entityColumn = "id"
    )
    val tenant: Tenant?
)
