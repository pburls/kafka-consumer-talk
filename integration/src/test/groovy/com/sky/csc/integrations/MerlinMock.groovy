package com.sky.csc.integrations

import com.sky.csc.Configuration
import com.sky.csc.metadata.ddi.DdiFragmentType
import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import org.awaitility.Awaitility

class MerlinMock {
    static final restClient = new RESTClient(Configuration.MerlinMockConfig.hostUrl)

    static Object getMerlinObject(DdiFragmentType fragmentType, String uuid) {
        // find a way to get any created merlin objects from the Merlin stub/mock
        def merlinRequest = null
        Awaitility.await().ignoreExceptionsMatching(MerlinMock.&isNotFoundException).until { merlinRequest = getMerlinRequestForId(uuid).responseData }
        return merlinRequest
    }

    static boolean isNotFoundException(Throwable e) {
        return e instanceof HttpResponseException && (e as HttpResponseException).getStatusCode() == 404
    }

    static getMerlinRequestForId(String id) {
        return restClient.get(path: Configuration.MerlinMockConfig.requestsEndpoint, query: ['id': id])
    }
}
