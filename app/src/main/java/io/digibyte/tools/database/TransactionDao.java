package io.digibyte.tools.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface TransactionDao {

    @Query("SELECT * FROM digi_transaction")
    List<DigiTransaction> getAll();

    @Query("SELECT * FROM digi_transaction WHERE tx_hash LIKE :txHash LIMIT 1")
    DigiTransaction findByTxHash(String txHash);

    @Insert
    void insertAll(DigiTransaction... transactions);

    @Delete
    void delete(DigiTransaction user);
}