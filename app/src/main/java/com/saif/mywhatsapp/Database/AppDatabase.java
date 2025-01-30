package com.saif.mywhatsapp.Database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.saif.mywhatsapp.Models.Message;
import com.saif.mywhatsapp.Models.Status;
import com.saif.mywhatsapp.Models.User;

@Database(entities = {User.class, Status.class, Message.class}, version = 9)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();
    public abstract StatusDao statusDao();
    public abstract MessageDao messageDao();
}
