package com.woxloi.custombiome.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.sql.Connection

/**
 * HikariCP ベースの MySQL 接続プロバイダ。
 * WoxloiDevAPI の MySQLProvider を置き換える自前実装。
 */
class MySQLProvider(
    private val host: String,
    private val port: Int,
    private val database: String,
    private val username: String,
    private val password: String,
    private val useSSL: Boolean = false,
    private val poolSize: Int = 10
) {
    private var dataSource: HikariDataSource? = null

    fun connect(): Boolean {
        return try {
            val config = HikariConfig().apply {
                jdbcUrl = "jdbc:mysql://$host:$port/$database" +
                        "?useSSL=$useSSL&characterEncoding=UTF-8&serverTimezone=UTC"
                this.username = this@MySQLProvider.username
                this.password = this@MySQLProvider.password
                maximumPoolSize = poolSize
                minimumIdle = 2
                connectionTimeout = 10_000
                idleTimeout = 600_000
                maxLifetime = 1_800_000
                poolName = "CustomBiome-Pool"
                addDataSourceProperty("cachePrepStmts", "true")
                addDataSourceProperty("prepStmtCacheSize", "250")
                addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
            }
            dataSource = HikariDataSource(config)
            // 接続テスト
            dataSource!!.connection.use { true }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getConnection(): Connection =
        dataSource?.connection ?: throw IllegalStateException("Database is not connected.")

    fun close() {
        dataSource?.close()
    }
}
