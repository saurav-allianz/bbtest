package a.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.core.io.support.ResourcePatternResolver
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import java.io.IOException
import javax.servlet.http.HttpServletRequest


@RestController
class SwaggerController {

  var swaggerUIFolder = "swagger-ui"

  // See https://allaroundjava.com/api-first-rest-service-swagger/
  @RequestMapping(method = [RequestMethod.GET], path = ["/v2/api-docs/{filename}"], produces = [MediaType.APPLICATION_JSON_VALUE])
  fun swaggerEndpoints(@PathVariable filename: String, request: HttpServletRequest): ByteArray {

    val basePath = request.getHeader("referer").split("swagger-ui.html")[0]
    val mapper = ObjectMapper(YAMLFactory())
    val root: ObjectNode = mapper.readTree(ClassPathResource("$swaggerUIFolder/$filename").inputStream) as ObjectNode
    root.putArray("servers").add(mapper.createObjectNode().put("url", basePath))
    return mapper.writer().writeValueAsBytes(root)
  }

  @RequestMapping(method = [RequestMethod.GET], path = ["/swagger-resources"], produces = [MediaType.APPLICATION_JSON_VALUE])
  @Throws(IOException::class)
  fun swaggerResources(): Any {
    val cl = this.javaClass.classLoader
    val resolver: ResourcePatternResolver = PathMatchingResourcePatternResolver(cl)
    val resources = listOf(*resolver.getResources("$swaggerUIFolder/swagger*.yaml"))
    return resources.stream()
      .map { obj: Resource -> obj.filename }
      .map { f: String ->
        mapOf(
          "name" to f,
          "url" to "/v2/api-docs/$f",
          "location" to "/",  // this should match the endpoint exposing Swagger JSON
          "swaggerVersion" to "3.0")
      }
      .toArray()
  }

  @RequestMapping(method = [RequestMethod.GET], path = ["/swagger-resources/configuration/security"], produces = [MediaType.APPLICATION_JSON_VALUE])
  fun swaggerSecurityConfig(): Any {
    return listOf(mapOf(
      "apiKeyVehicle" to "header",
      "scopeSeparator" to ",",
      "apiKeyName" to "api_key"))
  }

  @RequestMapping(method = [RequestMethod.GET], path = ["/swagger-resources/configuration/ui"], produces = [MediaType.APPLICATION_JSON_VALUE])
  fun swaggerUiConfig(): Any {
    return "{\"deepLinking\":true," +
      "\"displayOperationId\":false," +
      "\"defaultModelsExpandDepth\":1," +
      "\"defaultModelExpandDepth\":1," +
      "\"defaultModelRendering\":\"example\"," +
      "\"displayRequestDuration\":false," +
      "\"docExpansion\":\"none\"," +
      "\"filter\":false," +
      "\"operationsSorter\":\"alpha\"," +
      "\"showExtensions\":false," +
      "\"tagsSorter\":\"alpha\"," +
      "\"validatorUrl\":\"\"," +
      "\"apisSorter\":\"alpha\"," +
      "\"jsonEditor\":false," +
      "\"showRequestHeaders\":false," +
      "\"supportedSubmitMethods\":[\"get\",\"put\",\"post\",\"delete\",\"options\",\"head\",\"patch\",\"trace\"]}"
  }
}
