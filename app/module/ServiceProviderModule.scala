package module

import com.google.inject.AbstractModule
import services.{DbServiceAsync, SlickDbService}

class ServiceProviderModule extends AbstractModule {

  override def configure(): Unit = {
    bind(classOf[DbServiceAsync]).to(classOf[SlickDbService])
  }
}
