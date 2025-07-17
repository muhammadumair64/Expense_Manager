package com.iobits.budgetexpensemanager.localDatabase.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import com.iobits.budgetexpensemanager.localDatabase.entities.Profile

@Dao
interface ProfileDao {
    @Upsert
    fun insertProfile(profile: Profile)

    @Query("SELECT * FROM Profile")
    fun getProfile(): Profile

    @Query("DELETE FROM Profile")
    fun deleteAllProfiles()
}