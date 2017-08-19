package com.qnex.audioblocks.access.sheet

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.api.services.sheets.v4.model.ValueRange
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.nio.file.Path


class GoogleSheetProvider(private val clientSecretFilePath: Path) {

    companion object {
        /**
         * Application name.
         */
        private val APPLICATION_NAME = "Audioblocks access granting"

        /**
         * Directory to store user credentials for this application.
         */
        private val DATA_STORE_DIR = File(
                System.getProperty("user.home"), ".credentials/audioblocks-access-granting")

        /**
         * Global instance of the [FileDataStoreFactory].
         */
        private val DATA_STORE_FACTORY: FileDataStoreFactory

        /**
         * Global instance of the JSON factory.
         */
        private val JSON_FACTORY = JacksonFactory.getDefaultInstance()

        /**
         * Global instance of the HTTP transport.
         */
        private val HTTP_TRANSPORT: HttpTransport

        /**
         * Global instance of the scopes required by this quickstart.
         *
         *
         * If modifying these scopes, delete your previously saved credentials
         */
        private val ALLOWED_SCOPES = listOf(SheetsScopes.SPREADSHEETS)

        init {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport()
            DATA_STORE_FACTORY = FileDataStoreFactory(DATA_STORE_DIR)
        }
    }

    /**
     * Build and return an authorized Sheets API client service.

     * @return an authorized Sheets API client service
     * *
     * @throws IOException
     */
    val sheetsService: Sheets
        get() {
            val credential = authorize()
            return Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME)
                    .build()
        }

    /**
     * Creates an authorized Credential object.

     * @return an authorized Credential object.
     * *
     * @throws IOException
     */
    fun authorize(): Credential {
        val clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, BufferedReader(FileReader(clientSecretFilePath.toString())))

        // Build flow and trigger user authorization request.
        val flow = GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, ALLOWED_SCOPES)
                .setDataStoreFactory(DATA_STORE_FACTORY)
                .setAccessType("offline")
                .build()
        val credential = AuthorizationCodeInstalledApp(flow, LocalServerReceiver()).authorize("user")
        return credential
    }

    fun read(spreadsheetId: String, tabName: String, range: String): List<List<Any?>> {
        return sheetsService.spreadsheets().values()
                .get(spreadsheetId, tabName + "!" + range)
                .execute().getValues() ?: listOf()
    }

    fun read(spreadsheetId: String, tabName: String, rowIndex: Int): List<Any?> {
        checkRowIndex(rowIndex)
        return read(spreadsheetId, tabName, "A$rowIndex:O$rowIndex").first()
    }

    fun <T> read(spreadsheetId: String, tabName: String, rowIndex: Int, rangeReadMapper: RangeReadMapper<T>): T {
        return rangeReadMapper.apply(read(spreadsheetId, tabName, rowIndex))
    }

    fun <T> read(spreadsheetId: String, tabName: String, rangeReadMapper: RangeReadMapper<T>): List<T> {
        val values = read(spreadsheetId, tabName, rangeReadMapper.getRange())
        return values.map { rangeReadMapper.apply(it) }
    }

    fun update(spreadsheetId: String, tabName: String, range: String, values: List<List<Any?>>) {
        val body = ValueRange()
                .setValues(values)
        sheetsService.spreadsheets().values().update(spreadsheetId, tabName + "!" + range, body)
                .setValueInputOption("RAW")
                .execute()
    }

    fun update(spreadsheetId: String, tabName: String, rowIndex: Int, values: List<Any?>) {
        checkRowIndex(rowIndex)
        update(spreadsheetId, tabName, "A$rowIndex:O$rowIndex", listOf(values))
    }

    fun <T> update(spreadsheetId: String, tabName: String, rowIndex: Int,
                   rangeReadMapper: RangeWriteMapper<T>, value: T) {
        update(spreadsheetId, tabName, rowIndex, rangeReadMapper.apply(value))
    }

    fun <T> update(spreadsheetId: String, tabName: String, rangeReadMapper: RangeWriteMapper<T>, values: List<T>) {
        update(spreadsheetId, tabName, rangeReadMapper.getRange(), values.map { rangeReadMapper.apply(it) })
    }

    fun readAll(spreadsheetId: String, tabName: String,
                filter: IndexedReadFilter<List<Any?>>): IndexedIterator<List<Any?>> {

        return object : IndexedIterator<List<Any?>> {

            override fun incIndex() {
                rowIndex++
            }

            var values: List<Any?> = listOf()

            var rowIndex: Int = 1
            override fun hasNext(): Boolean {
                var run = false
                do {
                    run = !filter.preReadApply(rowIndex)
                    if (run) {
                        rowIndex++
                        continue
                    }
                    values = read(spreadsheetId, tabName, rowIndex)
                    if (values.isEmpty()) {
                        return false
                    }
                    run = !filter.postReadApply(rowIndex, values)
                } while (run)
                return true
            }

            override fun next(): List<Any?> {
                rowIndex++
                return values
            }

            override fun index(): Int {
                return rowIndex
            }
        }
    }

    fun <T> readAll(spreadsheetId: String, tabName: String, filter: IndexedReadFilter<T>,
                    rangeReadMapper: RangeReadMapper<T>): IndexedIterator<T> {


        val innerIterator = readAll(spreadsheetId, tabName, object: IndexedReadFilter<List<Any?>> {})

        return object : IndexedIterator<T> {

            override fun incIndex() {
                innerIterator.incIndex()
            }

            var mapped: T? = null

            override fun hasNext(): Boolean {
                var result = false
                do {
                    result = !filter.preReadApply(index())
                    if (result)  {
                        incIndex()
                        continue
                    }
                    if (!innerIterator.hasNext()) {
                        return false
                    }
                    mapped = rangeReadMapper.apply(innerIterator.next())

                    result = !filter.postReadApply(index(), mapped!!)
                } while (result)

                return true
            }


            override fun next(): T {
               return mapped!!
            }

            override fun index(): Int {
                return innerIterator.index()
            }
        }
    }

    private fun checkRowIndex(rowIndex: Int) {
        if (rowIndex < 1) {
            throw IllegalArgumentException("Row index must be great than 0")
        }
    }

    interface IndexedReadFilter<T> {

        fun preReadApply(index: Int): Boolean {
            return true
        }

        fun postReadApply(index: Int, model: T): Boolean {
            return true
        }
    }

    interface IndexedIterator<T>: Iterator<T> {
        fun index(): Int

        fun incIndex()
    }


}

