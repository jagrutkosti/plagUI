/**
 * Created by Jagrut on 07-06-2017.
 */
(function(){
    "use strict";
    /**
     * Service method to send requests to backend for all events related to uploading a document or text.
     */
    angular
        .module('plagUiApp')
        .factory('UploadDocsService', UploadDocsService);

    UploadDocsService.$inject = ['$http', '$timeout', 'Upload'];

    function UploadDocsService($http, $timeout, Upload) {
        const API_URL = '/api/plagchain/';
        var service = {};

        /**
         * Call UploadDocsREST.java#uploadDoc(fileToHash) and handle the callbacks
         * @param fileData the file data received from controller
         */
        service.uploadDocForBlockchain = function(fileData) {
            return Upload.upload({
                url: API_URL + 'uploadDoc',
                data: {fileToHash: fileData.fileToHash, isunpublished: fileData.isunpublished}
            }).then(function(response) {
                return response.data;
            }, function(response) {
                if(response.status > 0) {
                    return response.status + ':' + response.statusText;
                }
            });
        };

        /**
         * Call UploadDocsREST.java#uploadText(textToHash) and handle the callbacks
         * @param textToHash the file received from controller
         */
        service.uploadTextForBlockchain = function(textToHash) {
            var formData = new FormData();
            formData.append('textToHash', textToHash);
            return $http.post(API_URL + 'uploadText', formData, {
                transformRequest: angular.identity,
                headers: {'Content-Type': undefined}
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
