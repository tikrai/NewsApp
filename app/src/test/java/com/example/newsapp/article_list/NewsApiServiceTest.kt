package com.example.newsapp.article_list

import com.example.newsapp.NewsApiService
import com.example.newsapp.models.NewsApiResponse
import com.google.gson.JsonSyntaxException
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import java.net.HttpURLConnection.HTTP_BAD_REQUEST
import java.net.HttpURLConnection.HTTP_OK

class NewsApiServiceTest {
    private var mockWebServer = MockWebServer()
    private val mockUrl by lazy {
        mockWebServer.url("/").toString()
    }

    private val serializedResult = "{\n" +
            "    \"status\": \"ok\",\n" +
            "    \"totalResults\": 1,\n" +
            "    \"articles\": [\n" +
            "        {\n" +
            "            \"source\": {\n" +
            "                \"id\": \"SourceID\",\n" +
            "                \"name\": \"SourceName\"\n" +
            "            },\n" +
            "            \"author\": \"Author\",\n" +
            "            \"title\": \"Title\",\n" +
            "            \"description\": \"description\",\n" +
            "            \"url\": \"https://www.example.com\",\n" +
            "            \"urlToImage\": \"https://www.example.com/image.jpg\",\n" +
            "            \"publishedAt\": \"2019-11-12T04:46:00Z\",\n" +
            "            \"content\": \"Content\"\n" +
            "        }\n" +
            "    ]\n" +
            "}"

    @Before
    fun setup() {
        mockWebServer.start()
    }

    @After
    fun teardown() {
        mockWebServer.shutdown()
    }

    @Test
    fun shouldGetResultFromMockServerCorrectly() {
        val response = MockResponse().setResponseCode(HTTP_OK).setBody(serializedResult)
        mockWebServer.enqueue(response)

        val actual = NewsApiService.create(mockUrl)
            .getData("x", 1, 1, "x")
            .blockingFirst()

        val expected = NewsApiResponse.Result()
        Assert.assertEquals(expected, actual)
    }

    @Test(expected = JsonSyntaxException::class)
    fun shouldFailIfResponseStructureIsBad() {
        val body = "{\"status\":\"ok\",\"totalResults\":0,\"articles\":\"ok\"}"
        val response = MockResponse().setResponseCode(HTTP_OK).setBody(body)
        mockWebServer.enqueue(response)

        NewsApiService.create(mockUrl)
            .getData("x", 1, 1, "x")
            .blockingFirst()
    }

    @Test(expected = HttpException::class)
    fun shouldFailIfRequestParametersAreMissing() {
        val body = "{\"status\":\"error\",\"code\":\"parametersMissing\"}"
        val response = MockResponse().setResponseCode(HTTP_BAD_REQUEST).setBody(body)
        mockWebServer.enqueue(response)

        NewsApiService.create(mockUrl)
            .getData("x", 1, 1, "x")
            .blockingFirst()
    }
}
