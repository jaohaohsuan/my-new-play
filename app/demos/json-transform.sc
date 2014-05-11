import play.api.libs.json.Json

object jobs {

  import play.api.libs.json._
  import data._

  implicit def prettyPrint[a <: JsValue](s: JsResult[a]): String = Json.prettyPrint(s.get)

  implicit def jsonTransform[a <: JsValue](r: Reads[a]): String = json.transform(r)

  def pick: String = {
    (JsPath \ 'key2 \ 'key23).json.pick[JsArray]
  }

  def pickBranch: String = {
    (JsPath \ 'key2 \ 'key24 \ 'key241).json.pickBranch
  }

  def copyFrom: String = {
    (__ \ 'h1 \ 't1).json.copyFrom((__ \ 'key2 \ 'key22).json.pick)
  }

  def update: String = {
    val `append hello to key24` = JsPath.read[JsObject].map(ar => ar ++ Json.obj("hello" -> "world"))
    (__ \ 'key2 \ 'key24).json.update(`append hello to key24`)
  }

  def update2: String = {
    import play.api.libs.json.Reads._
    (__ \ 'key2 \ 'key21).json.update(
      of[JsNumber].map { case JsNumber(nb) => JsNumber(nb * 10)})
  }

  def put: String = {
    /*
    creates a new branch with a given value
      without taking into account input JSON
    */
    (__ \ 'key24 \ 'key241).json.put(JsNumber(1024))
  }

  def prune: String = {
    (__ \ 'key2).json.prune
  }

  def case7: String = {
    import play.api.libs.json.Reads._
    (__ \ 'key2).json.pickBranch(
      (__ \ 'key21).json.update(
        of[JsNumber].map { case JsNumber(nb) => JsNumber(nb + 10)}
      )
        andThen
        (__ \ 'key23).json.update(
          of[JsArray].map { case JsArray(arr) => JsArray(arr :+ JsString("delta"))}
        )
    )
  }
  def case8: String = {
    import play.api.libs.json.Reads._
    import play.api.libs.functional.syntax._
    val gizmo2gremlin = ((__ \ 'name).json.put(JsString("gremlin")) and
      (__ \ 'description).json.pickBranch(
          (__ \ 'size).json.update(of[JsNumber].map { case JsNumber(size) => JsNumber(size * 3)}) and
          (__ \ 'features).json.put(Json.arr("skinny", "ugly", "evil")) and
          (__ \ 'danger).json.put(JsString("always"))
          reduce
    ) and
    (__ \ 'hates).json.copyFrom((__ \ 'loves).json.pick)
    ) reduce

    gizmo.transform(gizmo2gremlin)
  }
}
jobs.pick
jobs.pickBranch






jobs.copyFrom






jobs.update
















jobs.update2





















jobs.put





jobs.prune






jobs.case7










jobs.case8












object data {
  val json = Json.parse(
    """{
        "key1" : "value1",
        "key2" : {
          "key21" : 123,
          "key22" : true,
          "key23" : [ "alpha", "beta", "gamma"],
          "key24" : {
            "key241" : 234.123,
            "key242" : "value242"
          }
        },
        "key3" : 234
      }""")

  val gizmo = Json.obj(
    "name" -> "gizmo",
    "description" -> Json.obj(
      "features" -> Json.arr("hairy", "cute", "gentle"),
      "size" -> 10,
      "sex" -> "undefined",
      "life_expectancy" -> "very old",
      "danger" -> Json.obj(
        "wet" -> "multiplies",
        "feed after midnight" -> "becomes gremlin"
      )
    ),
    "loves" -> "all"
  )
}











