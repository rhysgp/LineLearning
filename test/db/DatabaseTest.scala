package db

import javax.inject.Inject

import play.api.db.slick.DatabaseConfigProvider
import play.api.test.{PlaySpecification, WithApplication}
import services.SlickDbService

class DatabaseTest @Inject() (dbConfigProvider: DatabaseConfigProvider) extends PlaySpecification {

  "a user" should new WithApplication with WithDatabaseConfig {

    val dbService = new SlickDbService(dbConfigProvider)
    dbService.createDb()
    val futureTestUser = dbService.createUser("test@example.com")
    val testUser = await(futureTestUser)

    "have correct email address on creation" in { testUser.email must beEqualTo("test@example.com") }
    "have an id on creation" in {
      testUser.id must not(beNull)
      testUser.id.length must be_>(0)
    }

    "have unique id on creation" in {
      val futureTest2User = dbService.createUser("test2@example.com")
      val test2User = await(futureTest2User)

      test2User.id must not(beNull)
      test2User.id.length must be_>(0)
      test2User.id must not(be_==(testUser.id))
    }

    "be able to be retrieved by email" in {
      val futureTest3User = dbService.createUser("test@example.com")
      val test3User = await(futureTest3User)

      test3User.id must be_==(testUser.id)
      test3User.email must be_==(testUser.email)
    }
  }

//  "a scene" should new WithApplication with WithDatabaseConfig {
//
//    val dbService = new SlickDbService(dbConfig)
//    dbService.createDb()
//
//    "be able to be created" in {
//
//      val futureTestUser = dbService.addOrFindUser("test@example.com")
//      val testUser = await(futureTestUser)
//
//      dbService.
//    }
//  }
}
