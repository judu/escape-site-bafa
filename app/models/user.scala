package models

import javax.inject.Inject

import scala.util.{ Failure, Success }

import anorm._
import anorm.SqlParser.{ get, str }

import play.api.db.DBApi

import scala.concurrent.Future

case class Directeur(
  num_adh: String,
  piece_of_code: String,
  rep_enigme_a: Option[String],
  rep_enigme_b: Option[String],
  rep_enigme_c: Option[String],
  rep_enigme_d: Option[String],
  rep_enigme_e: Option[String],
  enigme_a_ok: Boolean = false,
  enigme_b_ok: Boolean = false,
  enigme_c_ok: Boolean = false,
  enigme_d_ok: Boolean = false,
  enigme_e_ok: Boolean = false
)

@javax.inject.Singleton
class DirecteurRepository @Inject()(dbapi: DBApi)(implicit ec: DatabaseExecutionContext) {

  private val db = dbapi.database("default")

  val parser = {
    str("num_adh") ~
    str("piece_of_code") ~
    get[Option[String]]("rep_enigme_a") ~
    get[Option[String]]("rep_enigme_b") ~
    get[Option[String]]("rep_enigme_c") ~
    get[Option[String]]("rep_enigme_d") ~
    get[Option[String]]("rep_enigme_e") ~
    get[Boolean]("enigme_a_ok") ~
    get[Boolean]("enigme_b_ok") ~
    get[Boolean]("enigme_c_ok") ~
    get[Boolean]("enigme_d_ok") ~
    get[Boolean]("enigme_e_ok") map {
      case num~poc~a~b~c~d~e~aok~bok~cok~dok~eok =>
        Directeur(num, poc, a, b, c, d, e, aok, bok, cok, dok, eok)
    }
  }


  def getDirecteur(num: String): Option[Directeur] = db.withConnection { implicit connection =>
    SQL("""SELECT num_adh ,piece_of_code ,rep_enigme_a ,rep_enigme_b ,rep_enigme_c
    ,rep_enigme_d ,rep_enigme_e ,enigme_a_ok ,enigme_b_ok ,enigme_c_ok ,enigme_d_ok ,enigme_e_ok
    FROM sgdf_users
    WHERE num_adh = {num_adh}""")
      .on('num_adh -> num)
      .as(parser.*)
      .headOption
  }

  def setEnigmeResponse(num: String, name: String, reponse: String) = db.withConnection { implicit connection =>
    SQL(s"UPDATE sgdf_users SET ${name} = {response} WHERE num_adh = {num}")
      .on('response -> reponse, 'num -> num)
      .execute()
  }

  def setEnigmeResponseOk(num: String, name: String) = db.withConnection { implicit connection =>
    SQL(s"UPDATE sgdf_users SET ${name} = true WHERE num_adh = {num}")
      .on('num -> num)
      .execute()
  }
}
