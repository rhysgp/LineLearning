package model

import play.api.mvc.Call

case class Navigation(userOpt: Option[db.User], items: Seq[NavItem])

case class NavItem(label: String, link: Call)
