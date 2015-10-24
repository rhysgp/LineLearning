package views

import controllers.routes
import model.{SceneName, Navigation, NavItem, User}

object NavigationHelper {

  private val home = NavItem("Home", routes.Application.index)

  def noNavigation = Navigation(None, Seq(home))

  def buildNavigation(userOpt: Option[User], showSceneNav: Boolean = true, sceneName: Option[SceneName] = None): Navigation = {
    Navigation(
      userOpt,
      Seq(
        Option(home),
        if (showSceneNav) Option(NavItem("Scenes", routes.ScenesController.list)) else None,
        sceneName.map(sn => NavItem(sn.name, routes.PromptController.list(sn.toString)))
      ).flatten
    )
  }
}