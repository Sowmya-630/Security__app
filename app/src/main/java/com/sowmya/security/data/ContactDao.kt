package com.sowmya.security.data

    import androidx.room.*
    import kotlinx.coroutines.flow.Flow

    @Dao
    interface ContactDao {
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insert(contact: ContactEntity)

        @Delete
        suspend fun delete(contact: ContactEntity)

        @Query("SELECT * FROM contacts")
        fun getAllContacts(): Flow<List<ContactEntity>>
    }
