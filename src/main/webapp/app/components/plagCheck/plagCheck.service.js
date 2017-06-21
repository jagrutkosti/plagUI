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

        service.checkForPlagiarism = function(data) {
            return Upload.upload({
                url : API_URL + 'plagCheckDoc',
                data : {plagCheckDoc: data.plagCheckDoc, checkUnpublishedWork: data.checkUnpublishedWork}
            }).then(function(response) {
                return response.data;
            }, function(response) {
                if(response.status > 0) {
                    return response.status + ':' + response.statusText;
                }
            });
        };

        return service;
    }
})();
