package com.qnex.audioblocks.access.client

import com.qnex.audioblocks.access.model.PartnerInfo
import org.apache.http.NameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.message.BasicNameValuePair
import java.util.*
import javax.annotation.concurrent.NotThreadSafe


@NotThreadSafe
class AudioblocksAccessProvidingClient(val audioblocksLoginName: String,
                                       val audioblocksPassword: String,
                                       val httpClient: CloseableHttpClient) {

    private var authorized = false

    private val loginUrl = "https://www.audioblocks.com/api/login/"

    private val sendEmailUrl = "https://www.audioblocks.com/api/???"


    fun authorize() {
        val post = HttpPost(loginUrl)

        val loginParameters = ArrayList<NameValuePair>()
        loginParameters.add(BasicNameValuePair("email", audioblocksLoginName))
        loginParameters.add(BasicNameValuePair("password", audioblocksPassword))

        post.entity = UrlEncodedFormEntity(loginParameters)

        val response = httpClient.execute(post)
        response.use {
            if (response.statusLine.statusCode != 302) {
                throw IllegalStateException("The client is not authorized!")
            }
        }
        authorized = true
    }

    fun sendEmail(partnerInfo: PartnerInfo) {
        if (!isAuthorized()) {
            authorize()
        }

        val post = HttpPost(sendEmailUrl)

        val partnerInfoParams = ArrayList<NameValuePair>()
        partnerInfoParams.add(BasicNameValuePair("email", partnerInfo.email))
        partnerInfoParams.add(BasicNameValuePair("name", partnerInfo.name))
        partnerInfoParams.add(BasicNameValuePair("lastName", partnerInfo.lastName))

        val response = httpClient.execute(post)

        response.use {
            if (response.statusLine.statusCode != 200) {
                throw IllegalStateException("Unable to send email message!")
            }
        }
    }

    private fun isAuthorized() = authorized

}

object Main {
    @Throws(Exception::class)
    @JvmStatic fun main(args: Array<String>) {
        AudioblocksAccessProvidingClient("admin@air.com", "Footage123!", HttpClients.createDefault())
                .authorize()
    }
}