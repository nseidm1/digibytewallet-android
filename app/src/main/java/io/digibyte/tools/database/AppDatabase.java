package io.digibyte.tools.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database(entities = {DigiTransaction.class}, version = 6)
public abstract class AppDatabase extends RoomDatabase {
    public abstract TransactionDao transactionDao();
}