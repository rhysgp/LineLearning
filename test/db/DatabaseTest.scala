package db

import model.User
import play.Application
import play.api.Play
import play.api.db.slick.DatabaseConfigProvider
import play.api.test.{WithApplication, PlaySpecification}
import services.SlickDbService
import slick.dbio.DBIO
import slick.driver.JdbcProfile

import scala.concurrent.Await

class DatabaseTest extends PlaySpecification {



//  private def recreateDbSchema(app: Application) = {
//    val dbConfig = DatabaseConfigProvider.get[JdbcProfile](app)
//    import dbConfig.driver.api._
//
//    val recreateSchema: DBIO[Unit] = DBIO.seq(
//      sqlu"drop schema public cascade",
//      sqlu"create schema public"
//    )
//    Await.ready(dbConfig.db.run(recreateSchema), 5 seconds)
//  }

  "creating a new user" should {

    "be successful" in new WithApplication with WithDatabaseConfig {

      val dbService = new SlickDbService(dbConfig)
      dbService.createDb()
      val futureTestUser = dbService.addOrFindUser1("test@example.com")

      val testUser = await(futureTestUser)

      testUser.email.address must beEqualTo("test@example.com")
      testUser.id must not(beNull)
      testUser.id.length must be_>(0)

    }
//    "have an id" in { testUser.get.id.length must be_>(0) }
//    "have the same email" in { testUser.get.email must beEqualTo("test@example.com") }
  }

}
