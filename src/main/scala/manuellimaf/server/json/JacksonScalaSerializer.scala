package manuellimaf.server.json

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper, PropertyNamingStrategy, SerializationFeature}
import com.fasterxml.jackson.module.afterburner.AfterburnerModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper

object JacksonScalaSerializer {

  private val mapper = new ObjectMapper() with ScalaObjectMapper
  mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
  mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
  mapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true)
  mapper.registerModule(new AfterburnerModule())
  mapper.registerModule(DefaultScalaModule)
  mapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES)

  def deserialize[T: Manifest](json: String): T = {
    mapper.readValue[T](json)
  }

  def serialize(content: AnyRef): String = {
    mapper.writeValueAsString(content)
  }
}