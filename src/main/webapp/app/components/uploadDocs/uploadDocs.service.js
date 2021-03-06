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

    UploadDocsService.$inject = ['Upload', '$http'];

    function UploadDocsService(Upload, $http) {
        const API_URL = '/api/plagchain/upload/';
        var service = {};

        /**
         * Call UploadDocsREST.java#uploadDoc(fileToHash) and handle the callbacks
         * @param fileData the file data received from controller
         * @param gRecaptchaResponse user response for recaptcha
         */
        service.uploadDocForBlockchain = function(fileData, gRecaptchaResponse) {
            return Upload.upload({
                url: API_URL + 'uploadDoc',
                data: {
                    fileToHash: fileData.fileToHash,
                    contactInfo: fileData.contactInfo,
                    decryptedPrivKey: fileData.decryptedPrivKey,
                    streamNames: angular.toJson(fileData.streamNames),
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
         * Call UploadDocsREST.java#uploadText(textToHash) and handle the callbacks
         * @param fileData the file data received from controller
         * @param gRecaptchaResponse user response for recaptcha
         */
        service.uploadTextForBlockchain = function(fileData, gRecaptchaResponse) {
            return Upload.upload({
                url: API_URL + 'uploadText',
                data: {
                    textToHash: fileData.textToHash,
                    fileName: fileData.fileName,
                    contactInfo: fileData.contactInfo,
                    decryptedPrivKey: fileData.decryptedPrivKey,
                    streamNames: angular.toJson(fileData.streamNames),
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
         * Call UploadDocsREST.java#uploadImage(imageToHash) and handle the callbacks
         * @param fileData the file data received from controller
         * @param gRecaptchaResponse user response for recaptcha
         */
        service.uploadImageForBlockchain = function(fileData, gRecaptchaResponse) {
            return Upload.upload({
                url : API_URL + 'uploadImage',
                data: {
                    imageToHash: fileData.imageToHash,
                    contactInfo: fileData.contactInfo,
                    decryptedPrivKey: fileData.decryptedPrivKey,
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
         * Call PermissionREST.java#getPermissionsForUser() and handle call backs
         */
        service.getPermissionsForUser = function() {
            return $http.get('/api/plagchain/getPermissionsForUser').then(function(response) {
                return response.data;
            }, function(response) {
                if(response.status > 0) {
                    return response.status + ':' + response.statusText;
                }
            })
        };

        /**
         * Retrieves the list of pd servers confirmed and stored in blockchain
         */
        service.getPdServersList = function() {
            return $http.get('/api/plagchain/getPdServersList').then(function(response) {
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
