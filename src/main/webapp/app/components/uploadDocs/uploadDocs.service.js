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

    UploadDocsService.$inject = ['Upload'];

    function UploadDocsService(Upload) {
        const API_URL = '/api/plagchain/upload/';
        var service = {};

        /**
         * Call UploadDocsREST.java#uploadDoc(fileToHash) and handle the callbacks
         * @param fileData the file data received from controller
         */
        service.uploadDocForBlockchain = function(fileData) {
            return Upload.upload({
                url: API_URL + 'uploadDoc',
                data: {
                    fileToHash: fileData.fileToHash,
                    contactInfo: fileData.contactInfo,
                    isunpublished: fileData.isunpublished
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
         */
        service.uploadTextForBlockchain = function(fileData) {
            return Upload.upload({
                url: API_URL + 'uploadText',
                data: {
                    textToHash: fileData.textToHash,
                    fileName: fileData.fileName,
                    contactInfo: fileData.contactInfo,
                    isunpublished: fileData.isunpublished
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
         */
        service.uploadImageForBlockchain = function(fileData) {
            return Upload.upload({
                url : API_URL + 'uploadImage',
                data: {
                    imageToHash: fileData.imageToHash,
                    contactInfo: fileData.contactInfo
                }
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
