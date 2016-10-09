package manuellimaf.server.json

trait Json {
  private val jsonSerializer = JacksonScalaSerializer

  protected def fromJson[T: Manifest](json: String): T = jsonSerializer.deserialize(json)
  protected def asJson[T: Manifest](obj: AnyRef): String = jsonSerializer.serialize(obj)
  
}