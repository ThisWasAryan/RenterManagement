package com.rms.app.core.model.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.rms.app.core.model.entities.Room
import com.rms.app.core.model.entities.Tenant
import com.rms.app.core.model.entities.Payment
import com.rms.app.core.model.entities.ElectricityReading

data class TenantWithRoom(
    @Embedded val tenant: Tenant,
    @Relation(
        parentColumn = "roomId",
        entityColumn = "id"
    )
    val room: Room?
)

data class TenantWithPayments(
    @Embedded val tenant: Tenant,
    @Relation(
        parentColumn = "id",
        entityColumn = "tenantId"
    )
    val payments: List<Payment>
)

data class TenantWithElectricity(
    @Embedded val tenant: Tenant,
    @Relation(
        parentColumn = "id",
        entityColumn = "tenantId"
    )
    val readings: List<ElectricityReading>
)

data class DocumentWithContext(
    @Embedded val document: com.rms.app.core.model.entities.Document,
    @Relation(
        parentColumn = "tenantId",
        entityColumn = "id"
    )
    val tenant: com.rms.app.core.model.entities.Tenant?
)
