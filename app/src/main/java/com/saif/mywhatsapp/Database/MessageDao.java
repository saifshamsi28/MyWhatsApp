package com.saif.mywhatsapp.Database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.saif.mywhatsapp.Models.Message;

import java.util.List;

@Dao
public interface MessageDao {
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        void insertMessage(Message message);

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        void insertAllMessages(List<Message> messages);

        @Query("SELECT * FROM messages WHERE `key` = :key LIMIT 1")
        Message getMessageByKey(String key);

        @Query("SELECT * FROM messages WHERE timeStamp = :timeStamp LIMIT 1")
        Message getMessageByTimeStamp(long timeStamp);

        @Query("UPDATE messages SET status = :status WHERE `key` = :key")
        void setMessageStatus(String key, int status);

        @Query("SELECT * FROM messages WHERE senderRoom = :senderRoom OR receiverRoom=:senderRoom ORDER BY timeStamp LIMIT 20")
        List<Message> getLast20Messages(String senderRoom);

        @Query("SELECT * FROM messages WHERE senderRoom = :senderRoom AND timeStamp > :lastFetchedTime ORDER BY timeStamp ASC")
        List<Message> getMessagesAfter(String senderRoom, long lastFetchedTime);

        @Query("DELETE FROM messages")
        void deleteAllMessages();
}
