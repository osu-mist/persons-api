package edu.oregonstate.mist.personsapi.db

import com.fasterxml.jackson.databind.ObjectMapper
import edu.oregonstate.mist.personsapi.core.JobObject
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.eclipse.jetty.http.HttpStatus
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.ws.rs.core.UriBuilder
import groovy.transform.InheritConstructors

@InheritConstructors
class MessageQueueDAOException extends Exception {}

class MessageQueueDAO {
    HttpClient httpClient
    URI requestURI

    MessageQueueDAO(HttpClient httpClient, String baseURL, String apiKey, String newJobEventType) {
        this.httpClient = httpClient

        def builder = UriBuilder.fromUri(baseURL.toURI())
        builder.queryParam("apikey", apiKey)
        builder.queryParam("eventType", newJobEventType)
        this.requestURI = builder.build()
    }

    private static Logger logger = LoggerFactory.getLogger(this)
    private static ObjectMapper objectMapper = new ObjectMapper()

    public void createNewJob(JobObject job, String employeeOsuID) throws MessageQueueDAOException {
        JobObjectMessage jobMessage = new JobObjectMessage(employeeOsuID: employeeOsuID, job: job)
        StringEntity requestBody = new StringEntity(objectMapper.writeValueAsString(jobMessage))
        requestBody.setContentType("application/json")

        HttpPost newJobRequest = new HttpPost(requestURI)
        newJobRequest.setEntity(requestBody)

        try {
            logger.info("sending a job message to the message queue")
            def response = httpClient.execute(newJobRequest)

            if (response.statusLine.statusCode != HttpStatus.CREATED_201) {
                logger.error("non-200 status {} when posting message", response.statusLine)
                throw new MessageQueueDAOException(
                        "bad http status code from message queue response")
            }
        } finally {
            // Unless the response entity is consumed in some way, the connection won't be released
            // to the pool unless its told to do so. Not returning it to the pool will result in an
            // exception after all the connections are occupied:
            // org.apache.http.conn.ConnectionPoolTimeoutException
            logger.info("release the connection back to the pool")
            newJobRequest.releaseConnection()
        }
    }
}

/**
 * Object used to represent new job object sent to message queue
 */
class JobObjectMessage {
    String employeeOsuID
    JobObject job
}
