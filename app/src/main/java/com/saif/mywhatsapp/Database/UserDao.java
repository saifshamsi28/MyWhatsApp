package com.saif.mywhatsapp.Database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.saif.mywhatsapp.Models.User;

import java.util.List;
@Dao
public interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUser(User user);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllUsers(List<User> users);

    @Query("SELECT * FROM users WHERE uid = :uid LIMIT 1")
    User getUserByUid(String uid);

    @Query("SELECT * FROM users WHERE name = :name")
    List<User> getUserByName(String name);

    @Query("DELETE FROM users")
    void deleteAllUsers();

    @Query("SELECT * FROM users")
    List<User> getAllUsers();

    @Query("SELECT COUNT(*) FROM users WHERE phoneNumber = :phoneNumber")
    int userExists(String phoneNumber);
}
