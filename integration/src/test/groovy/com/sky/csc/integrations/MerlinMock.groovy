package com.sky.csc.integrations

import com.sky.csc.Configuration;
import com.sky.csc.metadata.ddi.DdiFragmentType;
import groovyx.net.http.RESTClient;

class MerlinMock {
    static final restClient = new RESTClient(Configuration.MerlinMockConfig.hostUrl)

    static Object getMerlingObject(DdiFragmentType fragmentType, String uuid) {
        // find a way to get any created merlin objects from the Merlin stub/mock
        def response = restClient.get(path: Configuration.MerlinMockConfig.requestsEndpoint, query: ['id', uuid])
        return null;
    }
}
