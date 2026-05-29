package com.rms.app.core.model.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.rms.app.core.model.entities.Property
import com.rms.app.core.model.entities.Room

data class PropertyWithRooms(
    @Embedded val property: Property,
    @Relation(
        parentColumn = "id",
        entityColumn = "propertyId"
    )
    val rooms: List<Room>
)
