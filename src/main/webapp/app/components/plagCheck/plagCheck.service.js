/**
 * Created by Jagrut on 21-06-2017.
 */
(function() {
    "use strict";
    /**
     * Service method to send requests to backend for all events related to checking a document for plagiarism.
     */
    angular
        .module('plagUiApp')
        .factory('PlagCheckService', PlagCheckService);

    PlagCheckService.$inject = ['$http', 'Upload'];

    function PlagCheckService($http, Upload) {
        const API_URL = '/api/plagchain/';
        var service = {};

        /**
         * Call PlagCheckREST#plagCheckForDoc() and handle the call backs
         * @param data the data to be transferred, contains uploaded file and stream name to check
         * @param gRecaptchaResponse user response for google recaptcha
         */
        service.checkForPlagiarism = function(data, gRecaptchaResponse) {
            return Upload.upload({
                url : API_URL + 'plagCheckDoc',
                data : {
                    plagCheckDoc: data.plagCheckDoc,
                    checkUnpublishedWork: data.checkUnpublishedWork,
                    gRecaptchaResponse: gRecaptchaResponse
                }
            }).then(function(response) {
                return response.data;
            }, function(response) {
                if(response.status > 0) {
                    return response.status + ':' + response.statusText;
                }
            });
        };

        /**
         * Call PlagCheckRequestsREST#createPlagCheckRequest() and handle the call backs
         * @param simDocDetails the details of the document for which to generate the request
         * @param plagCheckDocFileName the name of the document for which the request was generated
         */
        service.createPlagCheckRequest = function(simDocDetails, plagCheckDocFileName) {
            return Upload.upload({
                url : API_URL + 'createPlagCheckRequest',
                data : {
                    simDocDetails : angular.toJson(simDocDetails),
                    plagCheckDocFileName : plagCheckDocFileName
                }
            }).then(function(response) {
                return response.data;
            }, function(response) {
                if(response.status > 0) {
                    return response.status + ':' + response.statusText;
                }
            })
        };

        return service;
    }
})();
