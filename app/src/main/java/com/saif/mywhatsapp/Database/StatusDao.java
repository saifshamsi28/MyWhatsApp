package com.saif.mywhatsapp.Database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.saif.mywhatsapp.Models.Status;

import java.util.List;

@Dao
public interface StatusDao {

    @Insert
    void insertStatus(Status status);

    @Update(onConflict =OnConflictStrategy.REPLACE)
    void updateStatus(Status status);

    @Query("SELECT * FROM Status WHERE userId = :userId")
    List<Status> getStatusesByUserId(String userId);

    @Query("DELETE FROM Status WHERE timeStamps = :timeStamps")
    void deleteStatusByTimeStamp(long timeStamps);
}

