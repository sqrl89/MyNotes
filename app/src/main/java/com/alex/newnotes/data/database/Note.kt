package com.alex.newnotes.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Notes")
data class Note(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    @ColumnInfo(name = "title")
    var title: String? = null,
    @ColumnInfo(name = "content")
    var content: String? = null,
    @ColumnInfo(name = "uri")
    var uri: String? = null,
    @ColumnInfo(name = "color")
    var color: String = "#373737",
    @ColumnInfo(name = "creation_date")
    var creationDate: String? = null,
    @ColumnInfo(name = "completed")
    var completed: Boolean = false,
    @ColumnInfo(name = "completion_date")
    var completionDate: String? = null,
    @ColumnInfo(name = "complete_by")
    var completeBy: String? = null

)