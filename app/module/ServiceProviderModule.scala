package module

import com.google.inject.AbstractModule
import play.api.db.slick.DatabaseConfigProvider
import services.{DbServiceAsync, SlickDbService}

class ServiceProviderModule extends AbstractModule {

  override def configure(): Unit = {

    // make services


  }
}
