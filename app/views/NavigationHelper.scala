package views

import controllers.routes
import model.{NavItem,Navigation}
import db._

object NavigationHelper {

  private val home = NavItem("Home", routes.Application.home())

  def noNavigation = Navigation(None, Seq(home.copy(active = true)))

  def buildSceneNavigation(userOpt: Option[User], showSceneNav: Boolean = true, sceneName: Option[Scene] = None): Navigation = {
    Navigation(
      userOpt,
      Seq(
        Option(home),
        if (showSceneNav) Option(NavItem("Scenes", routes.ScenesController.list(), active = sceneName.isEmpty)) else None,
        sceneName.map(sn => NavItem(sn.name, routes.PromptController.list(sn.id), active = true))
      ).flatten
    )
  }

  def buildHomeNavigation(userOpt: Option[User], showSceneNav: Boolean = true, sceneName: Option[Scene] = None): Navigation = {
    Navigation(
      userOpt,
      Seq(
        Option(home.copy(active = true)),
        if (showSceneNav) Option(NavItem("Scenes", routes.ScenesController.list())) else None,
        sceneName.map(sn => NavItem(sn.name, routes.PromptController.list(sn.id)))
      ).flatten
    )
  }
}