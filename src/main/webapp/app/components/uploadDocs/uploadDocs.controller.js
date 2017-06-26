/**
 * Created by Jagrut on 07-06-2017.
 */
(function(){
    "use strict";
    /**
     * Controller to handle all events related to uploading a document or a text
     */
    angular
        .module('plagUiApp')
        .controller('UploadDocsController', UploadDocsController);

    UploadDocsController.$inject = ['UploadDocsService', 'AlertService'];

    function UploadDocsController(UploadDocsService, AlertService){
        var vm = this;
        /**
         * fileData: {'fileToHash', 'textToHash', 'imageToHash', 'isunpublished', 'contactInfo', 'fileName', 'success', 'error'}
         */
        vm.fileData = {};
        vm.uploadDocForBlockchain = uploadDocForBlockchain;
        vm.uploadTextForBlockchain = uploadTextForBlockchain;
        vm.uploadImageForBlockchain = uploadImageForBlockchain;

        /**
         * Redirect the request to angular service after receiving the file from UI.
         * Handle the response from the server and redirect accordingly.
         */
        function uploadDocForBlockchain() {
            UploadDocsService.uploadDocForBlockchain(vm.fileData).then(function(response){
                if(response.success) {
                    vm.fileData.success = response.success;
                } else {
                    if(response.error)
                        vm.fileData.error = response.error;
                    else
                        vm.fileData.error = response;
                    AlertService.error(vm.fileData.error);
                }
            });
        }

        /**
         * Redirect the request to angular service after receiving the text from UI.
         * Handle the response from the server and redirect accordingly.
         */
        function uploadTextForBlockchain() {
            UploadDocsService.uploadTextForBlockchain(vm.fileData).then(function(response) {
                if(response.success) {
                    vm.fileData.success = response.success;
                } else {
                    if(response.error)
                        vm.fileData.error = response.error;
                    else
                        vm.fileData.error = response;
                    AlertService.error(vm.fileData.error);
                }
            });
        }

        /**
         * Redirect the request to angular service after receiving the image from UI.
         * Handle the response from the server and redirect accordingly.
         */
        function uploadImageForBlockchain() {
            UploadDocsService.uploadImageForBlockchain(vm.fileData).then(function(response) {
                if(response.success) {
                    vm.fileData.success = response.success;
                } else {
                    if(response.error)
                        vm.fileData.error = response.error;
                    else
                        vm.fileData.error = response;
                    AlertService.error(vm.fileData.error);
                }
            });
        }
    }
})();
