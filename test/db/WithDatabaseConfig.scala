package db

import play.api.Play
import play.api.db.slick.DatabaseConfigProvider
import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile

trait WithDatabaseConfig {
  lazy val (driver, db, dbConfig) = {
    val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)
    (dbConfig.driver, dbConfig.db, dbConfig)
  }
}
