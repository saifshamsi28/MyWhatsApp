package com.saif.mywhatsapp.Database;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import androidx.room.Room;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.room.RoomDatabase;

public class DatabaseClient {
    private Context mCtx;
    private static DatabaseClient mInstance;
    private AppDatabase appDatabase;

    private DatabaseClient(Context mCtx) {
        this.mCtx = mCtx;

        Migration MIGRATION_1_2 = new Migration(1, 2) {
            @Override
            public void migrate(SupportSQLiteDatabase database) {
                database.execSQL("CREATE TABLE IF NOT EXISTS users_new (phoneNumber TEXT NOT NULL PRIMARY KEY, uid TEXT, name TEXT, profileImage TEXT, about TEXT)");
                database.execSQL("INSERT OR IGNORE INTO users_new (phoneNumber, uid, name, profileImage, about) SELECT phoneNumber, uid, name, profileImage, about FROM users");
                database.execSQL("DROP TABLE IF EXISTS users");
                database.execSQL("ALTER TABLE users_new RENAME TO users");
            }
        };

        Migration MIGRATION_2_3 = new Migration(2, 3) {
            @Override
            public void migrate(SupportSQLiteDatabase database) {
                // Create the new table with the correct schema
                database.execSQL("CREATE TABLE IF NOT EXISTS Status_new (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "imageUrl TEXT, " +
                        "isLocal INTEGER NOT NULL, " +
                        "timeStamps INTEGER NOT NULL, " +
                        "userId TEXT)");

                // Check if the old Status table exists
                boolean statusTableExists = false;
                try (Cursor cursor = database.query("SELECT name FROM sqlite_master WHERE type='table' AND name='Status'")) {
                    if (cursor != null && cursor.moveToFirst()) {
                        statusTableExists = true;
                    }
                }

                // Copy data from the old table to the new table if it exists
                if (statusTableExists) {
                    database.execSQL("INSERT INTO Status_new (id, imageUrl, isLocal, timeStamps, userId) " +
                            "SELECT id, imageUrl, isLocal, timeStamps, userId FROM Status");
                    // Drop the old table
                    database.execSQL("DROP TABLE IF EXISTS Status");
                }

                // Rename the new table to the old table name
                database.execSQL("ALTER TABLE Status_new RENAME TO Status");
            }
        };

        Migration MIGRATION_3_4 = new Migration(3, 4) {
            @Override
            public void migrate(SupportSQLiteDatabase database) {
                // Add the new column 'status' to the users table
                database.execSQL("ALTER TABLE users ADD COLUMN status TEXT DEFAULT 'offline'");
            }
        };

        Migration MIGRATION_4_5 = new Migration(4,5) {
            @Override
            public void migrate(SupportSQLiteDatabase database) {
                // Add the new column 'status' to the users table
                database.execSQL("ALTER TABLE users ADD COLUMN fcmToken TEXT");
            }
        };

        Migration MIGRATION_5_6 = new Migration(5, 6) {
            @Override
            public void migrate(SupportSQLiteDatabase database) {
                // Create the new messages table
                database.execSQL("CREATE TABLE IF NOT EXISTS messages (" +
                        "timeStamp INTEGER PRIMARY KEY NOT NULL, " +
                        "message TEXT, " +
                        "senderId TEXT, " +
                        "status INTEGER NOT NULL, " +
                        "key TEXT, " +
                        "isAudioMessage INTEGER NOT NULL)");
            }
        };

        Migration MIGRATION_6_7 = new Migration(6, 7) {
            @Override
            public void migrate(SupportSQLiteDatabase database) {
                // Create the new messages table
                database.execSQL("CREATE TABLE IF NOT EXISTS messages (" +
                        "timeStamp INTEGER PRIMARY KEY NOT NULL, " +
                        "message TEXT, " +
                        "senderId TEXT, " +
                        "status INTEGER NOT NULL, " +
                        "key TEXT, " +
                        "isAudioMessage INTEGER NOT NULL, " +
                        "senderRoom TEXT)");
            }
        };

        Migration MIGRATION_7_8 = new Migration(7, 8) {
            @Override
            public void migrate(SupportSQLiteDatabase database) {
                // Create the new messages table
                database.execSQL("CREATE TABLE IF NOT EXISTS messages (" +
                        "timeStamp INTEGER PRIMARY KEY NOT NULL, " +
                        "message TEXT, " +
                        "senderId TEXT, " +
                        "status INTEGER NOT NULL, " +
                        "key TEXT, " +
                        "isAudioMessage INTEGER NOT NULL, " +
                        "senderRoom TEXT, " +
                        "receiverRoom TEXT)");
            }
        };

        Migration MIGRATION_8_9 = new Migration(8, 9) {
            @Override
            public void migrate(SupportSQLiteDatabase database) {
                // Create the new messages table
                database.execSQL("CREATE TABLE IF NOT EXISTS messages_new ("
                        + "timeStamp INTEGER PRIMARY KEY NOT NULL, "
                        + "message TEXT, "
                        + "senderId TEXT, "
                        + "status INTEGER NOT NULL, "
                        + "key TEXT, "
                        + "isAudioMessage INTEGER NOT NULL, "
                        + "senderRoom TEXT, "
                        + "receiverRoom TEXT)");

                // Create indices
                database.execSQL("CREATE INDEX index_senderRoom ON messages_new(senderRoom)");
                database.execSQL("CREATE INDEX index_receiverRoom ON messages_new(receiverRoom)");

                // Copy data from the old messages table to the new messages table
                database.execSQL("INSERT INTO messages_new (timeStamp, message, senderId, status, key, isAudioMessage, senderRoom, receiverRoom) "
                        + "SELECT timeStamp, message, senderId, status, key, isAudioMessage, senderRoom, receiverRoom FROM messages");

                // Drop the old messages table
                database.execSQL("DROP TABLE IF EXISTS messages");

                // Rename the new table to the old table name
                database.execSQL("ALTER TABLE messages_new RENAME TO messages");
            }
        };

        RoomDatabase.Callback dbCallback = new RoomDatabase.Callback() {
            @Override
            public void onOpen(SupportSQLiteDatabase db) {
                super.onOpen(db);
                // Check if the 'messages' table exists
                Cursor cursor = db.query("SELECT name FROM sqlite_master WHERE type='table' AND name='messages'");
                if (!cursor.moveToFirst()) {
                    // If the table does not exist, create it
                    db.execSQL("CREATE TABLE IF NOT EXISTS messages (" +
                            "timeStamp INTEGER PRIMARY KEY NOT NULL, " +
                            "message TEXT, " +
                            "senderId TEXT, " +
                            "status INTEGER NOT NULL, " +
                            "key TEXT, " +
                            "isAudioMessage INTEGER NOT NULL, " +
                            "senderRoom TEXT, " +
                            "receiverRoom TEXT)");
                }
                cursor.close();
            }
        };

        appDatabase = Room.databaseBuilder(mCtx, AppDatabase.class, "MyWhatsAppDB")
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9)
                .addCallback(dbCallback)
                .build();
    }

    public static synchronized DatabaseClient getInstance(Context mCtx) {
        if (mInstance == null) {
            mInstance = new DatabaseClient(mCtx);
        }
        return mInstance;
    }

    public AppDatabase getAppDatabase() {
        return appDatabase;
    }
}
