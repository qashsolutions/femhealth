package com.maa.health.data.local.database

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * Database migration tests
 *
 * Tests that:
 * - Migration from v1 to v2 preserves existing data
 * - New agentic system tables are created correctly
 * - Indices are created properly
 *
 * Note: Run with AndroidJUnit4 for instrumented testing
 */
@RunWith(AndroidJUnit4::class)
class MigrationTest {

    private val TEST_DB = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        MaaDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    @Throws(IOException::class)
    fun migrate1To2_createsAgenticTables() {
        // Create database with version 1 schema
        helper.createDatabase(TEST_DB, 1).apply {
            // Insert some test data in existing tables
            execSQL("""
                INSERT INTO users (id, name, email, phone, age, lifecycleStage, createdAt, lastSync)
                VALUES ('user-1', 'Test User', 'test@example.com', '+91999', 25, 'REPRODUCTIVE', 1704067200000, 1704067200000)
            """.trimIndent())
            close()
        }

        // Run migration
        val db = helper.runMigrationsAndValidate(TEST_DB, 2, true)

        // Verify new tables exist
        val cursor = db.query("SELECT name FROM sqlite_master WHERE type='table'")
        val tables = mutableListOf<String>()
        while (cursor.moveToNext()) {
            tables.add(cursor.getString(0))
        }
        cursor.close()

        assertTrue("user_interactions table should exist", tables.contains("user_interactions"))
        assertTrue("content_feedback table should exist", tables.contains("content_feedback"))
        assertTrue("observation_history table should exist", tables.contains("observation_history"))
        assertTrue("learned_preferences table should exist", tables.contains("learned_preferences"))

        db.close()
    }

    @Test
    @Throws(IOException::class)
    fun migrate1To2_preservesExistingData() {
        // Create database with version 1 and add data
        helper.createDatabase(TEST_DB, 1).apply {
            execSQL("""
                INSERT INTO users (id, name, email, phone, age, lifecycleStage, createdAt, lastSync)
                VALUES ('user-1', 'Test User', 'test@example.com', '+91999', 25, 'REPRODUCTIVE', 1704067200000, 1704067200000)
            """.trimIndent())
            close()
        }

        // Run migration
        val db = helper.runMigrationsAndValidate(TEST_DB, 2, true)

        // Verify existing data is preserved
        val cursor = db.query("SELECT * FROM users WHERE id = 'user-1'")
        assertTrue("User data should be preserved", cursor.moveToFirst())
        assertEquals("Test User", cursor.getString(cursor.getColumnIndex("name")))
        cursor.close()

        db.close()
    }

    @Test
    @Throws(IOException::class)
    fun migrate1To2_createsIndices() {
        // Create and migrate
        helper.createDatabase(TEST_DB, 1).close()
        val db = helper.runMigrationsAndValidate(TEST_DB, 2, true)

        // Check for indices
        val cursor = db.query("SELECT name FROM sqlite_master WHERE type='index'")
        val indices = mutableListOf<String>()
        while (cursor.moveToNext()) {
            indices.add(cursor.getString(0))
        }
        cursor.close()

        assertTrue(
            "Should have userId_timestamp index on user_interactions",
            indices.any { it.contains("user_interactions") && it.contains("userId") }
        )
        assertTrue(
            "Should have userId_contentId index on content_feedback",
            indices.any { it.contains("content_feedback") && it.contains("userId") }
        )

        db.close()
    }

    @Test
    @Throws(IOException::class)
    fun migrate1To2_newTablesHaveCorrectSchema() {
        // Create and migrate
        helper.createDatabase(TEST_DB, 1).close()
        val db = helper.runMigrationsAndValidate(TEST_DB, 2, true)

        // Verify user_interactions schema
        val cursor = db.query("PRAGMA table_info(user_interactions)")
        val columns = mutableMapOf<String, String>()
        while (cursor.moveToNext()) {
            val name = cursor.getString(cursor.getColumnIndex("name"))
            val type = cursor.getString(cursor.getColumnIndex("type"))
            columns[name] = type
        }
        cursor.close()

        assertEquals("TEXT", columns["id"])
        assertEquals("TEXT", columns["userId"])
        assertEquals("INTEGER", columns["timestamp"])
        assertEquals("TEXT", columns["interactionType"])

        db.close()
    }

    @Test
    @Throws(IOException::class)
    fun migrate1To2_canInsertIntoNewTables() {
        // Create and migrate
        helper.createDatabase(TEST_DB, 1).close()
        val db = helper.runMigrationsAndValidate(TEST_DB, 2, true)

        // Try inserting into new tables
        db.execSQL("""
            INSERT INTO user_interactions (id, userId, timestamp, interactionType, targetId, targetType, metadata, sessionId, durationMs)
            VALUES ('int-1', 'user-1', 1704067200000, 'MOOD_LOG', NULL, NULL, NULL, NULL, NULL)
        """.trimIndent())

        db.execSQL("""
            INSERT INTO content_feedback (id, userId, contentId, contentType, contentTopic, wasHelpful, feedbackText, timestamp)
            VALUES ('fb-1', 'user-1', 'content-1', 'ARTICLE', 'pregnancy', 1, 'Very helpful!', 1704067200000)
        """.trimIndent())

        db.execSQL("""
            INSERT INTO observation_history (id, userId, timestamp, observationType, observation, dataPointsAnalyzed, timeSpanDays, suggestsFollowUp, wasActedUpon, userFeedback)
            VALUES ('obs-1', 'user-1', 1704067200000, 'MOOD_PATTERN', 'Your mood is improving', 7, 7, 0, NULL, NULL)
        """.trimIndent())

        db.execSQL("""
            INSERT INTO learned_preferences (id, userId, preferenceType, preferenceKey, preferenceValue, confidence, lastUpdated, dataPoints)
            VALUES ('pref-1', 'user-1', 'CONTENT_TYPE', 'article', 'preferred', 0.8, 1704067200000, 5)
        """.trimIndent())

        // Verify insertions
        val cursor = db.query("SELECT COUNT(*) FROM user_interactions")
        cursor.moveToFirst()
        assertEquals(1, cursor.getInt(0))
        cursor.close()

        db.close()
    }
}
