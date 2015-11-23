package db

import play.api.test.{PlaySpecification, WithApplication}
import services.SlickDbService

class DatabaseTest extends PlaySpecification {

  "creating a new user" should new WithApplication with WithDatabaseConfig {

    val dbService = new SlickDbService(dbConfig)
    dbService.createDb()
    val futureTestUser = dbService.addOrFindUser1("test@example.com")
    val testUser = await(futureTestUser)

    "have correct email address" in { testUser.email.address must beEqualTo("test@example.com") }
    "should have an id" in {
      testUser.id must not(beNull)
      testUser.id.length must be_>(0)
    }

    "have unique id" in {
      val futureTest2User = dbService.addOrFindUser1("test2@example.com")
      val test2User = await(futureTest2User)

      test2User.id must not(beNull)
      test2User.id.length must be_>(0)
      test2User.id must not(be_==(testUser.id))
    }
  }
}
